/**
 * @file RemoveFriendModal.jsx
 * @description
 * Modal component for confirming and executing the removal of a friend.
 *
 * This modal is triggered when a user opts to remove a contact from their friends list.
 * It presents a confirmation prompt, performs the DELETE request, and displays status feedback.
 *
 * ### Responsibilities:
 * - Prompts the user to confirm the friend removal action.
 * - Sends a DELETE request to the backend via `apiServices` using the selected friend's ID.
 * - Displays success or error messages based on the result.
 * - Disables interaction during the removal process.
 *
 * ### Props:
 * - `isOpen` (boolean): Determines modal visibility.
 * - `onClose` (function): Callback for closing the modal.
 *
 * ### Context Used:
 * - **FriendContext**:
 *   - `selectedRecipient`: The friend selected for removal (includes `userName` and `id`).
 */


import {useContext, useEffect, useState} from "react";

import {FriendContext} from "../contex/FriendContext.jsx";

import apiServices from "../services/ApiServices.js";

import "./styles/RemoveFriendModal.css"

const RemoveFriendModal = ({isOpen, onClose}) => {

    const { selectedRecipient } = useContext(FriendContext);

    const [statusMessage, setStatusMessage] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const handleRemove = async (userId) => {
        setIsProcessing(true);

        const API_FRIEND_PATH = import.meta.env.VITE_API_BASE_FRIEND_URL;
        const REMOVE_URL = `${API_FRIEND_PATH}/removeFriend/${userId}`

        try {
            const response = await apiServices.delete(REMOVE_URL);

            setStatusMessage({
                text: response.message || "Friend removed successfully",
                isError: false
            });

            setTimeout(() => {
                onClose();
            }, 2000);

        } catch (error) {
            setStatusMessage({
                text: error.message || "Failed to remove friend",
                isError: true
            });
        } finally {
            setIsProcessing(false);
        }
    }

    useEffect(() => {
        if (isOpen) {
            document.body.classList.add('modal-open');
            setStatusMessage(null);
        } else {
            document.body.classList.remove('modal-open');
        }

        return () => {
            document.body.classList.remove('modal-open');
        };
    }, [isOpen]);

    if (!isOpen || !selectedRecipient) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-backdrop" onClick={onClose} />
            <div className="modal-content">
                <div className="modal-header">
                    <h1>Remove Friend</h1>
                    <button onClick={onClose} className="modal-close-button">✕</button>
                </div>
                <div className="modal-body">
                    {statusMessage ? (
                        <div className={`status-message ${statusMessage.isError ? 'error' : 'success'}`}>
                            {statusMessage.text}
                        </div>
                    ) : (
                        <>
                            <div className="confirmation-message">
                                <p>Are you sure you want to remove <strong>{selectedRecipient.userName}</strong> from your friends list?</p>
                                <p className="warning-text">This action cannot be undone.</p>
                            </div>
                            <div className="action-buttons remove-actions">
                                <button
                                    className="remove-btn"
                                    onClick={() => handleRemove(selectedRecipient.id)}
                                    disabled={isProcessing}
                                >
                                    {isProcessing ? "Removing..." : "Remove Friend"}
                                </button>
                                <button
                                    className="decline-btn"
                                    onClick={onClose}
                                    disabled={isProcessing}
                                >
                                    Cancel
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RemoveFriendModal;