/**
 * @file ContactList.jsx
 * @description
 * React component responsible for displaying the authenticated user's friends list.
 *
 * This component serves as the primary UI section for initiating conversations, viewing online statuses,
 * and managing friend interactions. It integrates real-time presence updates and message previews
 * using shared context state and backend data.
 *
 * ### Responsibilities:
 * - Fetches and displays a dynamic list of friends for the logged-in user.
 * - Handles message preview rendering and indicates whether a message is unseen.
 * - Tracks online/offline status per contact.
 * - Invokes message marking as "seen" when a user opens a chat.
 * - Triggers two modals:
 *   - `<AddFriendsModal />`: For sending friend requests.
 *   - `<NotificationsModal />`: For viewing incoming friend request alerts.
 * - Includes the `<UserProfile />` component, which displays the authenticated user's name.
 *
 * ### Contexts Used:
 * - **FriendContext**: Handles user list, selected chat recipient, online status, and last messages.
 * - **ProfileContext**: Provides current user's ID and contact profile pictures.
 * - **ModalContext**: Manages the open/close state of modals.
 * - **FeedbackContext**: Controls the friend request feedback flow (seen/accepted state).
 *
 * ### Dependencies:
 * - Uses `apiServices` for API calls.
 * - Relies on `handleCall` from `FriendsService.js` for fetching/updating contact info.
 * - Dynamically loads data using `useEffect` and memoizes data fetching with `useCallback`.
 */


import {useEffect, useContext, useCallback} from "react";

import UserProfile from "./UserProfile.jsx";
import AddFriendsModal from "./AddFriendsModal.jsx"
import NotificationsModal from "./NotificationsModal.jsx";

import apiServices from "../services/ApiServices.js";
import {handleCall} from "../services/FriendsService.js";

import { IoPeopleSharp } from "react-icons/io5";
import { IoIosNotifications } from "react-icons/io";
import { MdOutlineNotificationsActive } from "react-icons/md";

import {FeedbackContext} from "../contex/FeedbackContext.jsx";
import {ModalContext} from "../contex/ModalContext.jsx";
import {FriendContext} from "../contex/FriendContext.jsx";
import {ProfileContext} from "../contex/ProfileContext.jsx";

import "./styles/ContactList.css"

const ContactList = ({ onSelectRecipient}) => {

    const {
        lastMessages,
        selectedRecipient,
        onlineStatus,
        users,
        setUsers,setLastMessages,setOnlineStatus
    } = useContext(FriendContext);

    const {
        userProfilePicture,
        contactPictures,
        authUsersId
    } = useContext(ProfileContext);

    const {
        isAddModalOpen,
        setIsAddModalOpen,
        isNotificationModalOpen,
        setIsNotificationModalOpen
    } = useContext(ModalContext);

    const {
        acceptedRequest,
        setAcceptedRequest,
        seenRequest,
        setSeenRequest
    } = useContext(FeedbackContext)

    const isUnseenFromOther = (message) => {
        if (!message || !message.messageStatus || !message.senderId) return false;
        return message.messageStatus === "UNSEEN" && message.senderId !== authUsersId;
    };

    const markAsSeen = async (senderId, recipientId) => {
        if(!senderId || !recipientId) return;

        try{
            const API_CHAT_PATH = import.meta.env.VITE_API_BASE_CHAT_URL
            const MARK_AS_SEEN_URL = `${API_CHAT_PATH}/messages/markAsSeen/${senderId}/${recipientId}`
            await apiServices.post(MARK_AS_SEEN_URL);

            setLastMessages(prev => {
                const updated = { ...prev };
                if (updated[senderId]) {
                    updated[senderId] = { ...updated[senderId], messageStatus: "SEEN" };
                }
                return updated;
            });

        } catch (error){
            console.error("An error occurred while trying to mark messages as seen:", error.message)
        }
    }

    const handleUserClick = async (user) => {
        onSelectRecipient(user);

        const userMessage = lastMessages[user.id];
        if (userMessage && isUnseenFromOther(userMessage)) {
            await markAsSeen(user.id, authUsersId);
        }
    };

    const callAddModal = () =>{
        setIsAddModalOpen(true);
    }

    const callNotificationsModal = () =>{
        setIsNotificationModalOpen(true);
        setSeenRequest(false);
    }

    const loadFriends = useCallback(() => {
        if (authUsersId) {
            handleCall(setUsers, setOnlineStatus, authUsersId, setLastMessages);
        }
    }, [authUsersId, setUsers, setOnlineStatus, setLastMessages]);

    useEffect(() => {
        loadFriends();
    }, [loadFriends]);

    useEffect(() => {
        if (acceptedRequest) {
            loadFriends();
            setAcceptedRequest(false);
        }
    }, [acceptedRequest, setAcceptedRequest, loadFriends]);

    return(
        <div className="left-sidebar">
            <div className="sidebar-header">
                <div className="user-avatar placeholder">
                    {userProfilePicture}
                </div>
                <UserProfile/>
                <div className="add-fiends-button">
                    <button onClick={callAddModal}><IoPeopleSharp/></button>
                </div>
                <div className={seenRequest ? "unseen-notification-button" : "notification-button" }>
                    <button onClick={callNotificationsModal}>
                        {seenRequest ? <MdOutlineNotificationsActive/> : <IoIosNotifications/>}
                    </button>
                </div>
            </div>

            <AddFriendsModal
                isOpen={isAddModalOpen}
                onClose={()=>setIsAddModalOpen(false)}
            >
            </AddFriendsModal>

            <NotificationsModal
                isOpen={isNotificationModalOpen}
                onClose = {() => setIsNotificationModalOpen(false)}
            >
            </NotificationsModal>

            <div className="user-list">
                {users.length === 0 ? (
                    <div className="empty-list-container">
                        <div className="no-users-placeholder">
                            <p>Start your first chat by adding a friend!</p>
                            <button
                                className="add-friend-button"
                                onClick={()=>setIsAddModalOpen(true)}
                            >
                                Add Friend
                            </button>
                        </div>
                    </div>
                ) : (
                    users.map((user, i) => (
                        <div
                            key={user.id || i}
                            className={`user-item ${selectedRecipient?.id === user.id ? 'selected' : ''}`}
                            onClick={() => handleUserClick(user)}
                        >
                            <div className="user-avatar-container">
                                <div className="user-avatar placeholder-contact">
                                    {contactPictures[user.id] || user.userName.charAt(0).toUpperCase()}
                                </div>
                                <div className={`status-dot ${onlineStatus[user.id] === 'ONLINE' ? 'online' : 'offline'}`} />
                            </div>

                            <div className="user-info">
                                <h3 className="user-name">{user.userName}</h3>
                                <div className={`last-message ${isUnseenFromOther(lastMessages[user.id]) ? 'unseen' : ''}`}>
                                    <p>
                                        {lastMessages[user.id]?.senderId === authUsersId
                                            ? `You: ${lastMessages[user.id]?.content}`
                                            : lastMessages[user.id]?.content || "No messages yet"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    )

}

export default ContactList;