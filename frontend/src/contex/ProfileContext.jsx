/**
 * @file ProfileContext.jsx
 * @description
 * React Context for managing user profile-related state across the application.
 *
 * This context is storing information about a user like the authenticated users id or username
 *
 * ### Responsibilities:
 * - Stores and manages authenticated user details:
 *   - `authUsersId`: ID of the authenticated user.
 *   - `authUserName`: Username of the authenticated user.
 * - Handles profile-related data:
 *   - `profile`: Object containing detailed profile information.
 *   - `userProfilePicture`: URL or data string representing the user's profile picture.
 *   - `contactPictures`: Collection of images associated with the user's contacts.
 *
 * ## Usage
 * Wrap your root component with `<ProfileProvider>` and use `useContext(ProfileContext)` inside
 * any nested component to access the auth state and setters.
 */

import {useState, createContext} from "react";

export const ProfileContext = createContext();

export const ProfileProvider = ({ children }) => {

    const[authUsersId, setAuthUsersId] = useState(0)
    const[authUserName, setAuthUserName] = useState("")

    const [profile, setProfile] = useState(null);
    const [userProfilePicture, setUserProfilePicture] = useState("");
    const [contactPictures, setContactPictures] = useState("");

    return (
        <ProfileContext.Provider value={{
           profile, setProfile, userProfilePicture, setUserProfilePicture, contactPictures,
            setContactPictures, authUsersId, setAuthUsersId, authUserName, setAuthUserName
        }}>
            {children}
        </ProfileContext.Provider>
    );
};