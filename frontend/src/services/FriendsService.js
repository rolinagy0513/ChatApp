/**
 * @file FriendsService.js
 *
 * Provides logic for retrieving the current user's friends, their online status,
 * and the last message exchanged with each friend.
 *
 * Dependencies:
 * - apiServices: handles HTTP requests with credentials and error handling.
 *
 * Environment Variables:
 * - VITE_API_BASE_FRIEND_URL: base path for the friend API.
 * - VITE_API_BASE_CHAT_URL: base path for chat-related API calls.
 */


import apiServices from "./ApiServices.js";

/**
 * Fetches the last message exchanged between the authenticated user and a specific friend.
 * Updates the `setLastMessages` state with the latest content, sender, and status.
 *
 * @param {string|number} userId - The friend's user ID.
 * @param {Function} setLastMessages - State setter to update the lastMessages object.
 * @param {string|number} authUsersId - The ID of the authenticated user.
 *
 * @returns {Promise<void>}
 */

const fetchLastMessage = async (userId, setLastMessages, authUsersId) => {
    if (!userId || !authUsersId) {
        console.warn("⚠️ fetchLastMessage: Missing required parameters", { userId, authUsersId });
        return;
    }

    try {
        const API_CHAT_PATH=import.meta.env.VITE_API_BASE_CHAT_URL
        const LAST_MESSAGE_URL = `${API_CHAT_PATH}/lastMessage/${authUsersId}/${parseInt(userId)}`

        const response = await apiServices.get(LAST_MESSAGE_URL);

        setLastMessages(prev => ({
            ...prev,
            [userId]: {
                senderId: response.senderId,
                content: response.content || "No messages yet",
                messageStatus: response.messageStatus || "UNSEEN"
            }
        }));
    } catch (error) {
        console.error("❌ Error fetching last message for user", userId, ":", error.message);
        setLastMessages(prev => ({
            ...prev,
            [userId]: {
                content: "No messages yet",
                messageStatus: "SEEN",
                senderId: null
            }
        }));
    }
};

/**
 * Retrieves the current user's friends from the backend, including their online status
 * and last message information. This function is typically used to populate the sidebar
 * or refresh the main contact list.
 *
 * @param {Function} setUsers - State setter for updating the full user list.
 * @param {Function} setOnlineStatus - State setter for mapping userId => online status.
 * @param {string|number} authUsersId - The ID of the authenticated user.
 * @param {Function} setLastMessages - State setter for storing last messages per user.
 *
 * @returns {Promise<void>}
 */

export const handleCall = async (setUsers, setOnlineStatus, authUsersId, setLastMessages) => {
    if (!setUsers || !setOnlineStatus || !authUsersId || !setLastMessages) {
        console.error("❌ handleCall: Missing required parameters!");
        return;
    }

    try {
        const API_FRIEND_PATH = import.meta.env.VITE_API_BASE_FRIEND_URL;
        const FRIENDS_URL = `${API_FRIEND_PATH}/getFriends`;
        const users = await apiServices.get(FRIENDS_URL);

        if (!users || users.length === 0) {
            setUsers([]);
            setOnlineStatus({});
            setLastMessages({});
            return;
        }

        setUsers(users);

        const initialStatus = {};

        for (const user of users) {
            initialStatus[user.id] = user.liveStatus;
        }
        setOnlineStatus(initialStatus);

        for (const user of users) {
            try {
                await fetchLastMessage(user.id, setLastMessages, authUsersId);
            } catch (error) {
                setLastMessages(prev => ({
                    ...prev,
                    [user.id]: {
                        content: "No messages yet",
                        messageStatus: "SEEN",
                        senderId: null
                    }
                }));
            }
        }
    } catch (error) {
        console.error("❌ Error in handleCall:", error.message);
    }
};