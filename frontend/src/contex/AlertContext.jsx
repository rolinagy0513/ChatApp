/**
 * @file AlertContext.jsx
 * @description
 * React Context for managing real-time alert state within the application.
 *
 * This context handles the display of alerts related to:
 * - Incoming friend requests
 * - Responses to sent friend requests (accepted or rejected)
 *
 * ## Usage
 * Wrap your root component with `<AlertProvider>` and use `useContext(AlertContext)` inside
 * any nested component to access the alert state and setters.
 */


import {useState, createContext} from "react";

export const AlertContext = createContext();

export const AlertProvider = ({ children }) => {

    const [incomingRequest, setIncomingRequest] = useState(null);
    const [showRequestAlert, setShowRequestAlert] = useState(false);

    const [incomingResponse, setIncomingResponse] = useState(null);
    const [showResponseAlert, setShowResponseAlert] = useState(false);

    return (
        <AlertContext.Provider value={{
            incomingRequest, setIncomingRequest,
            showRequestAlert, setShowRequestAlert,
            incomingResponse, setIncomingResponse,
            showResponseAlert, setShowResponseAlert
        }}>
            {children}
        </AlertContext.Provider>
    );
};