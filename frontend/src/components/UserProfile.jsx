/**
 * @file UserProfile.jsx
 * @description
 * React component responsible for fetching and displaying the authenticated user's profile information.
 *
 * This component retrieves the logged-in user's data from the backend and stores relevant
 * information in the shared `ProfileContext`.
 *
 * ### Responsibilities:
 * - Makes an API call to fetch the currently authenticated user's profile.
 *
 * ### Contexts Used:
 * - **ProfileContext**: Updates and accesses the user's profile, username, ID, and avatar placeholder.
 *
 * ### Dependencies:
 * - Uses `apiServices` to call the `/me` endpoint from the users service.
 */


import {useEffect, useContext} from "react";

import apiServices from "../services/ApiServices.js";

import {ProfileContext} from "../contex/ProfileContext.jsx";

const UserProfile = () => {

    const {profile, setProfile,
        setUserProfilePicture,
        setAuthUsersId,
        setAuthUserName
    } = useContext(ProfileContext);

    useEffect(() => {
        const API_USERS_PATH = import.meta.env.VITE_API_BASE_USERS_URL
        const PROFILE_URL = `${API_USERS_PATH}/me`;

        apiServices.get(PROFILE_URL)
            .then(data => {
                const newProfile = {
                    userName: data.userName,
                    id: data.id
                };
                setAuthUsersId(data.id);
                setAuthUserName(data.userName);
                setProfile(newProfile);
                setUserProfilePicture(data.userName.charAt(0).toUpperCase());
            })
            .catch(error => console.error("Error loading profile:", error));
    }, []);


    return (
        <div className="current-user">
            {profile ? (
                <div className="profile-info">
                    <div className="user-userName">
                        <h2>{profile.userName}</h2>
                    </div>
                </div>
            ) : (
                <p>Loading profile...</p>
            )}
        </div>
    );
};

export default UserProfile;