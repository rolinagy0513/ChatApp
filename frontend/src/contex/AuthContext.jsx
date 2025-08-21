/**
 * @file FriendContext.jsx
 * @description
 * React Context for managing UI feedback states throughout the application.
 *
 * This context provides states to the component
 *
 * ### Responsibilities:
 * - Maintains the `selectedRecipient` state to track the currently active chat user.
 *   This value is typically set in the `ContactList.jsx` component.
 * - Tracks real-time user presence using the `onlineStatus` state.
 * - Stores the full user list in the `users` state, useful for populating UI elements like contact lists.
 * - Holds the `lastMessages` map to quickly access recent messages exchanged with users.
 *
 * ## Usage
 * Wrap your root component with `<FriendProvider>` and use `useContext(FriendContext)` inside
 * any nested component to access the auth state and setters.
 */


import {useState, createContext} from "react";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {

    const [userProfilePicture, setUserProfilePicture] = useState("");
    const [registerFormData, setRegisterFormData] = useState({});
    const [loginFormData, setLoginFormData] = useState({});


    return (
        <AuthContext.Provider value={{
            registerFormData, setRegisterFormData,
            loginFormData, setLoginFormData,
            userProfilePicture, setUserProfilePicture
        }}>
            {children}
        </AuthContext.Provider>
    );
};