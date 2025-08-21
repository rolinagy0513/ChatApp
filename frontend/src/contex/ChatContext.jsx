/**
 * @file ChatContext.jsx
 * @description
 * React Context for managing chat-related state within the application.
 *
 * This context is responsible on setting the chat messages states mainly used in ChatWindow.jsx component.
 *
 * ### Responsibilities:
 * - Stores and updates the current list of chat `messages`.
 * - Manages the value of the `newMessage` input field.
 * - Provides a `messagesEndRef` reference to automatically scroll to the latest message in the chat.
 * - Maintains a `shouldScrollRef` flag to conditionally control scroll behavior based on user interaction.
 *
 * ## Usage
 * Wrap your root component with `<ChatProvider>` and use `useContext(ChatContext)` inside
 * any nested component to access the chat state and setters.
 */


import {createContext, useRef, useState} from "react";

export const ChatContext = createContext()

export const  ChatProvider = ({children}) => {

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const messagesEndRef = useRef(null);
    const shouldScrollRef = useRef(true);

    return (
        <ChatContext.Provider value={
            {
                messages, setMessages,
                newMessage, setNewMessage,
                messagesEndRef,shouldScrollRef
            }
        }>
            {children}
        </ChatContext.Provider>
    );

};