/**
 * @file ChatWindow.jsx
 * @description
 * React component responsible for displaying the chat conversation between the authenticated user
 * and the currently selected contact.
 *
 * This component renders the chat UI, including message bubbles, timestamps, and a message input field.
 * It supports real-time message delivery via WebSocket and ensures conversations stay updated and
 * scrolled to the bottom. The selected chat recipient is determined in the ContactList and passed via context.
 *
 * ### Responsibilities:
 * - Displays the chat history between the authenticated user and the selected recipient.
 * - Listens to WebSocket events to receive and render live messages.
 * - Manages optimistic message rendering and deduplication.
 * - Automatically scrolls to the most recent message.
 * - Provides an input field and send button to compose and send messages.
 * - Notifies the parent via `updateLastMessage` prop to keep last message previews in sync.
 *
 * ### Contexts Used:
 * - **FriendContext**: Supplies the `selectedRecipient`, representing the currently active conversation.
 * - **ProfileContext**: Provides the authenticated user's ID.
 * - **ChatContext**: Manages the message list, input state, and scroll behavior.
 *
 * ### Dependencies:
 * - Uses `apiServices` to fetch historical chat messages.
 * - Relies on `websocketServices` to handle real-time messaging.
 * - Performs multiple `useEffect` hooks for data loading, WebSocket subscription, and scroll behavior.
 */


import {useEffect,useContext} from 'react';

import {ProfileContext} from "../contex/ProfileContext.jsx";
import {FriendContext} from "../contex/FriendContext.jsx";
import {ChatContext} from "../contex/ChatContext.jsx";

import apiServices from '../services/ApiServices.js';
import websocketServices from '../services/WebsocketServices.js';

import './styles/ChatWindow.css';


const ChatWindow = ({updateLastMessage}) => {
    const { authUsersId } = useContext(ProfileContext);
    const {selectedRecipient} = useContext(FriendContext)
    const {messages, setMessages, newMessage, setNewMessage, messagesEndRef, shouldScrollRef} = useContext(ChatContext)

    const API_CHAT_PATH = import.meta.env.VITE_API_BASE_CHAT_URL

    useEffect(() => {
        if (selectedRecipient && authUsersId) {
            const userIds = [authUsersId, selectedRecipient.id].sort((a, b) => a - b);
            apiServices.get(`${API_CHAT_PATH}/messages/${userIds[0]}/${userIds[1]}`)
                .then(history => {
                    setMessages(history || []);
                    shouldScrollRef.current = true;
                })
                .catch(error => {
                    console.error('Error fetching messages:', error);
                    setMessages([]);
                });
        } else {
            setMessages([]);
        }
    }, [selectedRecipient?.id, authUsersId]);

    useEffect(() => {
        if (!authUsersId || !selectedRecipient?.id || !websocketServices.isConnected) {
            return;
        }

        const subscribeUrl = `/user/${authUsersId}/queue/messages`;

        const handleMessage = (notification) => {
            const notificationSenderId = parseInt(notification.senderId);
            const notificationRecipientId = parseInt(notification.recipientId);
            const currentUserId = parseInt(authUsersId);
            const selectedRecipientId = parseInt(selectedRecipient.id);

            const isCurrentConversation =
                (notificationSenderId === currentUserId && notificationRecipientId === selectedRecipientId) ||
                (notificationSenderId === selectedRecipientId && notificationRecipientId === currentUserId);

            if (isCurrentConversation) {
                if (updateLastMessage) {
                    updateLastMessage(selectedRecipientId, notification.senderId, notification.content, notification.messageStatus);
                }

                setMessages(prev => {
                    const exists = prev.some(msg => msg.id === notification.id);
                    if (exists) return prev;

                    const newMsg = {
                        id: notification.id,
                        sender: { id: notificationSenderId },
                        recipient: { id: notificationRecipientId },
                        content: notification.content,
                        timestamp: notification.timestamp || new Date().toISOString()
                    };

                    const filtered = notificationSenderId === currentUserId
                        ? prev.filter(msg =>
                            !(msg.isOptimistic &&
                                msg.content === notification.content &&
                                msg.sender?.id === currentUserId))
                        : prev;

                    shouldScrollRef.current = true;
                    return [...filtered, newMsg];
                });
            } else {
                const otherUserId = notificationSenderId === currentUserId
                    ? notificationRecipientId
                    : notificationSenderId;
                if (updateLastMessage) {
                    updateLastMessage(otherUserId, notification.senderId, notification.content, notification.messageStatus);
                }
            }
        };


        const subscription = websocketServices.subscribe(subscribeUrl, handleMessage);

        return () => subscription?.unsubscribe();

    }, [authUsersId, selectedRecipient?.id]);




    useEffect(() => {
        if (shouldScrollRef.current) {
            scrollToBottom();
            shouldScrollRef.current = false;
        }
    }, [messages]);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    const handleSend = () => {
        if (!newMessage.trim() || !selectedRecipient) return;

        const content = newMessage.trim();
        const recipientId = parseInt(selectedRecipient.id);
        const chatMessage = {
            recipient: { id: recipientId },
            content
        };

        const success = websocketServices.sendMessage('/app/chat', chatMessage);

        if (success) {
            setNewMessage('');
            if (updateLastMessage) {
                updateLastMessage(recipientId, authUsersId, content, "SEEN");
            }
        } else {
            alert('Failed to send message. Please check your connection.');
        }
    };

    const handleKeyPress = e => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    if (!selectedRecipient) {
        return (
            <div className="chat-area empty-chat">
                <div className="empty-chat-message">
                    <h3>Welcome to ChatApp</h3>
                    <p>Select a contact to start messaging</p>
                </div>
            </div>
        );
    }

    return (
        <div className="chat-area">
            <div className="chat-header">
                <div className="current-user">
                    <div className="user-avatar placeholder">
                        {selectedRecipient.userName.charAt(0).toUpperCase()}
                    </div>
                    <h3>{selectedRecipient.userName}</h3>
                </div>
            </div>

            <div className="messages-container">
                {messages.map((message, index) => {
                    const senderId = message.sender?.id || message.senderId;
                    const isSent = Number(senderId) === Number(authUsersId);

                    return (
                        <div
                            key={message.id || `msg-${index}`}
                            className={`message ${isSent ? 'sent' : 'received'}`}
                        >
                            <div className="message-bubble">{message.content}</div>
                            <span className="message-time">
                                {new Date(message.timestamp).toLocaleTimeString([], {
                                    hour: '2-digit',
                                    minute: '2-digit'
                                })}
                            </span>
                        </div>
                    );
                })}
                <div ref={messagesEndRef} />
            </div>

            <div className="message-input">
                <input
                    type="text"
                    value={newMessage}
                    onChange={e => setNewMessage(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="Type a message..."
                />
                <button
                    className="send-button"
                    onClick={handleSend}
                    disabled={!newMessage.trim()}
                >
                    Send
                </button>
            </div>
        </div>
    );
};

export default ChatWindow;
