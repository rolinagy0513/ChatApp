/**
 * @file Main.jsx
 *
 * Main component of the ChatApp application.
 *
 * Serves as the central layout and logic hub for the chat interface, including:
 * - Fetching and displaying contacts
 * - Opening chat windows
 * - Displaying user info
 * - Handling real-time WebSocket communication for:
 *   - Friend requests
 *   - Friend responses
 *   - Online status updates
 *   - Friend removal
 *
 * Uses multiple contexts to manage shared application state:
 * - ProfileContext for authentication details
 * - FeedbackContext for UI indicators (e.g., loading, seen requests)
 * - FriendContext for contact-related state (e.g., selected chat, friend list)
 * - AlertContext for displaying real-time alerts
 *
 * Sets up and cleans up WebSocket subscriptions based on the current authenticated user.
 *
 * Exports: Main (React component)
 */


import {useState, useContext, useEffect, useCallback, useRef, useMemo} from "react";
import ContactList from "../components/ContactList.jsx";
import ChatWindow from "../components/ChatWindow.jsx";
import UserInfoSidebar from "../components/UserInfoSidebar.jsx";

import {ProfileContext} from "../contex/ProfileContext.jsx";
import {FeedbackContext} from "../contex/FeedbackContext.jsx";
import {FriendContext} from "../contex/FriendContext.jsx";
import {AlertContext} from "../contex/AlertContext.jsx";

import websocketServices from "../services/WebsocketServices.js";

import {handleCall} from "../services/FriendsService.js";

import "./styles/Main.css";


const Main = () => {

    const {authUsersId} = useContext(ProfileContext);
    const {setSeenRequest} = useContext(FeedbackContext)

    const {
        setSelectedRecipient,
        setOnlineStatus,
        setLastMessages,
        setUsers,
        setUserInfoRefreshTrigger
    } = useContext(FriendContext);

    const {
        incomingRequest, setIncomingRequest,
        showRequestAlert, setShowRequestAlert,
        incomingResponse, setIncomingResponse,
        showResponseAlert, setShowResponseAlert
    } = useContext(AlertContext)


    const stateRefs = useRef({
        setUsers,
        setOnlineStatus,
        setLastMessages
    });

    const subscriptions = useRef({
        request: null,
        response: null,
        status: null,
        removal: null
    });

    const subscriptionUrls = useMemo(() => ({
        request: `/user/${authUsersId}/queue/requests`,
        response: `/user/${authUsersId}/queue/request-responses`,
        status: `/user/${authUsersId}/queue/isOnline`,
        removal: `/user/${authUsersId}/queue/friendRemoval`
    }), [authUsersId]);


    useEffect(() => {
        stateRefs.current = {
            setUsers,
            setOnlineStatus,
            setLastMessages
        };
    }, [setUsers, setOnlineStatus, setLastMessages]);

    const sockUrl = import.meta.env.VITE_API_WEBSOCKET_BASE_URL

    const refreshFriendsList = useCallback(() => {
        if (authUsersId && stateRefs.current.setUsers && stateRefs.current.setOnlineStatus && stateRefs.current.setLastMessages) {
            handleCall(
                stateRefs.current.setUsers,
                stateRefs.current.setOnlineStatus,
                authUsersId,
                stateRefs.current.setLastMessages
            );
        } else {
            console.warn("⚠️ Cannot refresh friends list - missing dependencies");
        }
    }, [authUsersId]);


    useEffect(() => {
        if (authUsersId) {
            refreshFriendsList();
        }
    }, [authUsersId, refreshFriendsList]);

    useEffect(() => {
        if (!authUsersId) {
            console.log("⚠️ No authUsersId, skipping WebSocket setup");
            return;
        }

        websocketServices.connect(sockUrl, {
            onConnect: () => {
                console.log("✅ WebSocket connected");

                subscriptions.current.request = websocketServices.subscribe(subscriptionUrls.request, handleFriendRequest);
                subscriptions.current.response = websocketServices.subscribe(subscriptionUrls.response, handleResponse);
                subscriptions.current.status = websocketServices.subscribe(subscriptionUrls.status, handleLiveStatus);
                subscriptions.current.removal= websocketServices.subscribe(subscriptionUrls.removal, handleRemoval);

                setTimeout(() => {
                    refreshFriendsList();
                }, 100);
            },
            onDisconnect: () => {
                console.log("❌ WebSocket disconnected");
                Object.values(subscriptions.current).forEach(sub => sub = null);
            },
            onError: (e) => {
                console.error('❌ WebSocket error:', e);
                Object.values(subscriptions.current).forEach(sub => sub = null);
            },
        });

        return () => {
            console.log("🧹 Cleaning up WebSocket subscriptions...");

            if (subscriptions.current.request ||
                subscriptions.current.response ||
                subscriptions.current.status ||
                subscriptions.current.removal){
                Object.values(subscriptions.current).forEach(sub => sub = null);
            }

            websocketServices.disconnect();
        };
    }, [authUsersId, refreshFriendsList])

    const updateLastMessage = (userId, senderId, content, messageStatus = 'UNSEEN') => {
        setLastMessages(prev => {
            const newState = {
                ...prev,
                [userId]: {
                    senderId,
                    content,
                    messageStatus
                }
            };
            return newState;
        });
    };

    const handleFriendRequest = useCallback((message) => {

        setIncomingRequest(message);
        setShowRequestAlert(true);
        setSeenRequest(true);

        setTimeout(() => {
            setShowRequestAlert(false);
        }, 5000);
    }, [setIncomingRequest, setShowRequestAlert, setSeenRequest]);

    const handleResponse = useCallback((response) => {

        setIncomingResponse(response);

        setTimeout(() => {
            refreshFriendsList();
        }, 50);

        if (response.senderId === authUsersId) {

            setShowResponseAlert(true)

            setTimeout(() => {
                setShowResponseAlert(false);
            }, 5000);
        }

    }, [authUsersId, refreshFriendsList, setIncomingResponse, setShowResponseAlert]);

    const handleLiveStatus = useCallback((status) => {
        setOnlineStatus(prev => ({
            ...prev,
            [status.id]: status.liveStatus
        }));
    }, [setOnlineStatus]);

    const handleRemoval = useCallback(() => {
        setTimeout(() => {
            refreshFriendsList();
            setSelectedRecipient(null);
            setUserInfoRefreshTrigger(prev => !prev);
        }, 50);
    }, [refreshFriendsList, setSelectedRecipient,setUserInfoRefreshTrigger]);

    return(
        <div className="main-container">
            {showRequestAlert && incomingRequest && (
                <div className="friend-request-alert">
                    <div className="alert-content">
                        <span className="alert-text">
                            <p>{incomingRequest.content}</p>
                            <p className="alert-user-name">{incomingRequest.senderName}</p>
                        </span>
                        <span className="alert-hint">Check your notifications</span>
                    </div>
                    <button
                        className="alert-close"
                        onClick={() => setShowRequestAlert(false)}
                    >
                        x
                    </button>
                </div>
            )}

            {showResponseAlert && incomingResponse &&(
                <div className={`friend-response-alert ${incomingResponse.content.includes('Accepted') ? 'accepted' : 'rejected'}`}>
                    <div className="alert-content">
                        <span className="alert-text">
                            <p><strong>{incomingResponse.senderName}</strong> {incomingResponse.content}</p>
                            {incomingResponse.friendsSince && (
                                <p className="alert-friends-since">
                                    Friends since: {new Date(incomingResponse.friendsSince).toLocaleDateString()}
                                </p>
                            )}
                        </span>
                    </div>
                    <button
                        className="alert-close"
                        onClick={() => setShowResponseAlert(false)}
                    >
                        x
                    </button>
                </div>
            )}

            <div className="sidebar left-sidebar">
                <ContactList onSelectRecipient={setSelectedRecipient}/>
            </div>


            <div className="chat-area">
                <ChatWindow updateLastMessage={updateLastMessage}/>
            </div>


            <div className="sidebar right-sidebar">
                <UserInfoSidebar/>
            </div>

        </div>
    );

};

export default Main;