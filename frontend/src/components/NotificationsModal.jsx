/**
 * @file NotificationsModal.jsx
 * @description
 * Modal component for displaying and managing incoming friend requests.
 *
 * This modal is triggered from the UI notifications icon and
 * presents a list of pending friend requests fetched from the backend.
 * Users can accept or reject requests, with responses sent via WebSocket.
 *
 * ### Responsibilities:
 * - Fetches pending friend requests from the backend API when opened.
 * - Displays a list of requests with sender info and timestamp.
 * - Allows the user to **accept** or **reject** each request.
 * - Sends response through WebSocket to `/app/friend-request-response`.
 *
 * ### Props:
 * - `isOpen` (boolean): Determines modal visibility.
 * - `onClose` (function): Closes the modal and resets related states.
 *
 * ### Context Used:
 * - **FeedbackContext**:
 *   - `setAcceptedRequest`: Signals that a friend request has been accepted.
 */


import {useState, useEffect, useContext} from "react";

import {FeedbackContext} from "../contex/FeedbackContext.jsx";

import apiServices from "../services/ApiServices.js";
import websocketServices from "../services/WebsocketServices.js";

import "./styles/NotificationsModal.css"

const NotificationsModal = ({isOpen, onClose}) =>{

    const{setAcceptedRequest} = useContext(FeedbackContext)

    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const API_FRIEND_PATH = import.meta.env.VITE_API_BASE_FRIEND_URL;
    const REQUEST_URL = `${API_FRIEND_PATH}/getRequests`

    const fetchNotifications = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await apiServices.get(REQUEST_URL);
            setNotifications(response);
        } catch (error) {
            setError("Failed to load notifications");
            console.error("Notification error:", error);
        } finally {
            setLoading(false);
        }
    };


    const handleAccept  = (requestId, senderId) =>{

        const response = {
            requestId: requestId,
            senderId: senderId,
            status: "ACCEPTED"
        }

        const success = websocketServices.sendMessage("/app/friend-request-response",response)

        if (success){
            removeNotification(requestId)
            setAcceptedRequest(true)
        }else{
            console.error("Problem at the websocket send response")
        }

    }

    const handleReject  = (requestId, senderId) =>{

        const response = {
            requestId: requestId,
            senderId: senderId,
            status: "REJECTED"
        }

        const success = websocketServices.sendMessage("/app/friend-request-response",response)

        if (success){
            removeNotification(requestId)
        }else{
            console.error("Problem at the websocket send response")
        }

    }

    const removeNotification = (requestId) => {
        setNotifications(prev => prev.filter(n => n.id !== requestId));
    };

    useEffect(() => {
        if (isOpen) {
            document.body.classList.add('modal-open');
            fetchNotifications();
        } else {
            document.body.classList.remove('modal-open');
        }

        return () => {
            document.body.classList.remove('modal-open');
        };
    }, [isOpen]);

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-backdrop" onClick={onClose} />
            <div className="modal-content">
                <div className="modal-header">
                    <h1>Friend Requests</h1>
                    <button onClick={onClose} className="modal-close-button">✕</button>
                </div>
                <div className="modal-body">
                    {loading ? (
                        <div className="loading-indicator">Loading notifications...</div>
                    ) : error ? (
                        <div className="error-container">
                            <p className="error-message">{error}</p>
                            <button onClick={fetchNotifications} className="retry-button">
                                Retry
                            </button>
                        </div>
                    ) : notifications && notifications.length > 0 ? (
                        <div className="results-container">
                            {notifications.map(notification => (
                                <div key={notification.id} className="notification-item">
                                    <div className="user-avatar-notifications">
                                        {notification.senderName.charAt(0).toUpperCase()}
                                    </div>
                                    <div className="user-details">
                                        <div className="user-name">{notification.senderName}</div>
                                        <div className="request-time">
                                            Received at: {new Date(notification.sentAt).toLocaleString()}
                                        </div>
                                    </div>
                                    <div className="action-buttons">
                                        <button
                                            className="accept-btn"
                                            onClick={() => handleAccept(notification.id, notification.senderId)}
                                        >
                                            Accept
                                        </button>
                                        <button
                                            className="decline-btn"
                                            onClick={() => handleReject(notification.id, notification.senderId)}
                                        >
                                            Reject
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="no-results">No pending friend requests</p>
                    )}
                </div>
            </div>
        </div>
    );

}

export default NotificationsModal