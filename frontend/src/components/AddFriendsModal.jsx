/**
 * @file AddFriendsModal.jsx
 * @description
 * Modal component for initiating friend requests by searching users by username.
 *
 * This modal appears when the user clicks the "add friend" icon in the ContactList component.
 * It enables users to input a search term, perform a backend query for matching users, and send
 * a friend request through WebSocket once a user is selected.
 *
 * ### Responsibilities:
 * - Opens a modal overlay when `isOpen` is true and disables background scroll.
 * - Allows users to search other users by username using the backend API.
 * - Displays matched results in a scrollable list format with avatars.
 * - Sends a friend request using WebSocket once the user clicks the "Add" button.
 * - Prevents duplicate friend requests and provides visual feedback for sent requests.
 * - Closes the modal on backdrop or close button interaction.
 *
 * ### Props:
 * - `isOpen` (boolean): Controls visibility of the modal.
 * - `onClose` (function): Callback to close the modal.
 *
 * ### Dependencies:
 * - **apiServices**: Sends POST request to search users by name.
 * - **websocketServices**: Sends friend requests in real-time via WebSocket.
 * - **react-icons**: Displays checkmark once the request is sent.
 *
 */

import React, { useEffect, useState } from "react";

import apiServices from "../services/ApiServices.js";
import websocketServices from "../services/WebsocketServices.js";

import { IoMdCheckmark } from "react-icons/io";

import "./styles/AddFriendsModal.css";

const AddFriendsModal = ({ isOpen, onClose }) => {

    const [searchQuery, setSearchQuery] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const[isSent, setIsSent] = useState([]);

    const API_FRIEND_PATH = import.meta.env.VITE_API_BASE_FRIEND_URL;
    const ADD_FRIEND_URL = `${API_FRIEND_PATH}/searchUser`;

    const sendRequestUrl = `/app/friend-request`

    const handleSearch = async () => {

        if (!searchQuery.trim()) {
            setSearchResults([]);
            return;
        }

        setIsSearching(true);

        console.log(`The search term is this: ${searchQuery}`)

        try{
            const response = await apiServices.post(ADD_FRIEND_URL,{
                userName: searchQuery
            })
            console.log(response)
            setSearchResults(response || []);

        }catch(error){
            console.error(`Search failed:  ${error.message}`)
            setSearchResults([]);
        }finally {
            setIsSearching(false);
        }


    };

    const sendRequest = (userId) =>{

        const request = {
            recipientId: userId
        }

        try{
            websocketServices.sendMessage(sendRequestUrl,request)
            setIsSent(prev => [...prev, userId]);
        }catch (error){
            console.error(error.message)
        }
    }

    useEffect(() => {
        if (isOpen) {
            document.body.classList.add('modal-open');
        } else {
            document.body.classList.remove('modal-open');
            setSearchQuery("");
            setSearchResults([]);
        }

        return () => {
            document.body.classList.remove('modal-open');
        };
    }, [isOpen]);

    if(!isOpen) {
        return null;
    }

    return(
        <div className="modal-overlay">
            <div className="modal-backdrop" onClick={onClose} />
            <div className="modal-content">
                <div className="modal-header">
                    <h1>Add friends</h1>
                    <button onClick={onClose} className="modal-close-button">✕</button>
                </div>
                <div className="modal-body">
                    <div className="search-form">
                        <input
                            type="text"
                            className="search-input"
                            placeholder="Search users..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        />
                        <button
                            className="search-button"
                            onClick={handleSearch}
                            disabled={isSearching}
                        >
                            {isSearching ? 'Searching...' : 'Search'}
                        </button>
                    </div>

                    {isSearching ? (
                        <div className="loading-indicator">Loading...</div>
                    ) : searchResults.length > 0 ? (
                        <div className="results-container">
                            {searchResults.map(user => (
                                <div
                                    key={user.id}
                                    className="user-result-item"
                                >
                                    <div className="user-result-avatar">
                                        {user.userName.charAt(0).toUpperCase()}
                                    </div>
                                    <div className="user-result-info">
                                        <div className="user-result-name">{user.userName}</div>
                                        <button
                                            onClick={() => !isSent.includes(user.id) && sendRequest(user.id)}
                                            className={isSent.includes(user.id) ? "sent-button" : "add-button"}
                                            disabled={isSent.includes(user.id)}
                                        >
                                            {isSent.includes(user.id) ? <IoMdCheckmark size={20} /> : 'Add'}
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="no-results">
                            {searchQuery ? 'No users found' : 'Enter a name to search for users'}
                        </p>
                    )}
                </div>
            </div>
        </div>
    )
}

export default AddFriendsModal;