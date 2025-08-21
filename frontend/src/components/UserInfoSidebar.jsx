/**
 * @file UserInfoSidebar.jsx
 * @description
 * React component responsible for displaying detailed information about the selected chat recipient.
 *
 * This component serves as a sidebar UI for viewing real-time status and user metadata of the contact
 * currently in conversation. It conditionally fetches and presents extended contact information and
 * allows interaction for removing the friend from the contact list.
 *
 * ### Responsibilities:
 * - Displays basic recipient info: username, online/offline status, and avatar.
 * - Dynamically fetches and renders additional contact details (e.g., name, email, friendship date).
 * - Opens the `<RemoveFriendModal />` to initiate a friend removal action.
 *
 * ### Contexts Used:
 * - **FriendContext**: Retrieves the selected recipient and their current online status.
 * - **ModalContext**: Controls the visibility of the remove friend modal.
 *
 * ### Dependencies:
 * - Uses `apiServices` to fetch extended contact data via the friends service.
 * - Imports and triggers `<RemoveFriendModal />` for handling contact deletion flow.
 * - Uses `useEffect` for lifecycle-based data loading and `useContext` for shared state access.
 */


import {useContext, useEffect, useState} from "react";

import { FriendContext } from "../contex/FriendContext.jsx";
import {ModalContext} from "../contex/ModalContext.jsx";

import apiServices from "../services/ApiServices.js";

import RemoveFriendModal from "./RemoveFriendModal.jsx";

import "./styles/UserInfoSidebar.css"

const UserInfoSidebar = () => {
    const { selectedRecipient, onlineStatus,userInfoRefreshTrigger } = useContext(FriendContext);
    const {isRemoveModalOpen, setIsRemoveModalOpen} = useContext(ModalContext);

    const [userInfo, setUserInfo] = useState(null);

    const recipientStatus = selectedRecipient?.id
        ? onlineStatus[selectedRecipient.id]
        : null;

    useEffect(() => {
        if (selectedRecipient?.id) {
            const fetchContactInfo = async () => {
                try {
                    const API_FRIEND_PATH = import.meta.env.VITE_API_BASE_FRIEND_URL;
                    const INFO_URL = `${API_FRIEND_PATH}/getOne/${selectedRecipient.id}`;
                    const response = await apiServices.get(INFO_URL);
                    setUserInfo(response);
                } catch (error) {
                    console.error(error.message);
                    setUserInfo(null);
                }
            };
            fetchContactInfo();
        } else {
            setUserInfo(null);
        }
    }, [selectedRecipient?.id, userInfoRefreshTrigger])

    if (!selectedRecipient) {
        return (
            <div className="user-info-sidebar">
                <div className="placeholder-message">
                    Select a contact to view details
                </div>
            </div>
        );
    }


    const formatFriendsSince = (dateString) => {
        if (!dateString) return "N/A";
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    const callRemoveModal = () =>{
        setIsRemoveModalOpen(true);
    }

    return (
        <div className="user-info-sidebar">
            <div className="user-profile-info">
                <div className="user-avatar placeholder-info">
                    {userInfo?.firstName?.charAt(0) || ''}
                </div>
                <h2 className="user-name-info">{userInfo?.userName || 'Loading...'}</h2>
                <p className="user-status-info" style={{
                    color: recipientStatus === 'ONLINE' ? '#00cc66' : '#a0a0a0'
                }}>
                    {recipientStatus === 'ONLINE' ? 'Online' : 'Offline'}
                </p>
            </div>

            {isRemoveModalOpen && (
                <RemoveFriendModal isOpen={isRemoveModalOpen} onClose={()=>setIsRemoveModalOpen(false)}/>
            )}

            {userInfo && (
                <div className="user-details-info">
                    <h3>Contact Information</h3>
                    <div className="detail-item">
                        <strong>First name:</strong>
                        <span>{userInfo.firstName}</span>
                    </div>
                    <div className="detail-item">
                        <strong>Last name:</strong>
                        <span>{userInfo.lastName}</span>
                    </div>
                    <div className="detail-item">
                        <strong>Email:</strong>
                        <span>{userInfo.email}</span>
                    </div>
                    <div className="detail-item">
                        <strong>Friends since:</strong>
                        <span>{formatFriendsSince(userInfo.friendsSince)}</span>
                    </div>
                </div>
            )}

            <div className="remove-friend-button">
                <button onClick={callRemoveModal}>Remove Friend</button>
            </div>

        </div>
    );
};

export default UserInfoSidebar;