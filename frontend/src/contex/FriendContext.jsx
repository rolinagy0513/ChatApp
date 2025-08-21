/**
 * @file FriendContext.jsx
 * @description
 * React Context for managing UI feedback states throughout the application.
 *
 * This context sets user data like last message or online status
 *
 * ### Responsibilities:
 * - Controls the `isLoading` state to indicate ongoing operations and display loading indicators.
 * - Manages success and error messages via `message` and `error` states.
 * - Tracks the status of friend request interactions using:
 *   - `acceptedRequest`: Indicates whether a friend request has been accepted.
 *   - `seenRequest`: Flags that a friend request has been acknowledged or seen.
 *   - `userInfoRefreshTrigger`: Tracks that the friend was removed and if yes than calls the component refresh
 *
 ## Usage
 Wrap your root component with `<FriendProvider>` and use `useContext(FriendContext)` inside
 any nested component to access the auth state and setters.
 */

import { useState, createContext} from "react";

export const FriendContext = createContext();

export const FriendProvider = ({ children }) => {

    const [selectedRecipient, setSelectedRecipient] = useState(null);
    const [onlineStatus, setOnlineStatus] = useState({});
    const [users, setUsers] = useState([]);
    const [lastMessages, setLastMessages] = useState({});
    const [userInfoRefreshTrigger, setUserInfoRefreshTrigger] = useState(false);

    return (
        <FriendContext.Provider value={{
            selectedRecipient, setSelectedRecipient,
            onlineStatus, setOnlineStatus,
            users, setUsers,
            lastMessages, setLastMessages,
            userInfoRefreshTrigger, setUserInfoRefreshTrigger
        }}>
            {children}
        </FriendContext.Provider>
    );
};