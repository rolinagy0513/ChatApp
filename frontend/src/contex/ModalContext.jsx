/**
 * @file ModalContext.jsx
 * @description
 * React Context for managing modal visibility states throughout the application.
 *
 * This context sets the modal states which are determining that a modal is Open or Closed (visible or not)
 *
 * ### Responsibilities:
 * - Controls visibility of different modals through individual boolean states:
 *   - `isAddModalOpen`: Tracks whether the "Add" modal is open.
 *   - `isNotificationModalOpen`: Indicates if the notification modal is active.
 *   - `isRemoveModalOpen`: Manages the state for the "Remove" modal dialog.
 * - Provides corresponding setter functions to toggle each modal’s visibility.
 *
 * ## Usage
 * Wrap your root component with `<ModalProvider>` and use `useContext(ModalContext)` inside
 * any nested component to access the auth state and setters.
 */


import {useState, createContext} from "react";

export const ModalContext = createContext();

export const ModalProvider = ({ children }) => {

    const[isAddModalOpen, setIsAddModalOpen] = useState(false);
    const[isNotificationModalOpen, setIsNotificationModalOpen] = useState(false);
    const[isRemoveModalOpen, setIsRemoveModalOpen] = useState(false);

    return (
        <ModalContext.Provider value={{
           isAddModalOpen, setIsAddModalOpen,
            isNotificationModalOpen,
            setIsNotificationModalOpen,
            isRemoveModalOpen,
            setIsRemoveModalOpen
        }}>
            {children}
        </ModalContext.Provider>
    );
};