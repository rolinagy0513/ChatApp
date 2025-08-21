/**
 * @file FeedbackContext.jsx
 * @description
 * React Context for managing UI feedback states throughout the application.
 *
 * This context provides a centralized way to manage feedback-related state,
 * allowing components to respond to various user interaction.
 *
 * ### Responsibilities:
 * - Controls the `isLoading` state to indicate ongoing operations and display loading indicators.
 * - Manages success and error messages via `message` and `error` states.
 * - Tracks the status of friend request interactions using:
 *   - `acceptedRequest`: Indicates whether a friend request has been accepted.
 *   - `seenRequest`: Flags that a friend request has been acknowledged or seen.
 *
 ## Usage
 Wrap your root component with `<FeedbackProvider>` and use `useContext(FeedbackContext)` inside
 any nested component to access the auth state and setters.
 */


import {createContext, useState} from "react";

export const FeedbackContext = createContext()

export const  FeedBackProvider = ({children}) => {

    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [acceptedRequest, setAcceptedRequest] = useState(false);
    const [seenRequest, setSeenRequest] = useState(false);

    return (
        <FeedbackContext.Provider value={
            {
                isLoading,setIsLoading,
                message,setMessage,
                error,setError,
                acceptedRequest,setAcceptedRequest,
                seenRequest, setSeenRequest
            }
        }>
            {children}
        </FeedbackContext.Provider>
    );

};