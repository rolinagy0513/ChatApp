/**
 * @file RoutesConfig.jsx
 * @description
 * This file defines route configurations for the application using React Router v6.
 * It separates the routing logic into two route groups:
 * - `AuthRoutes` for authentication-related pages (login, register)
 * - `ProfileRoutes` for user profile and contact-related pages
 *
 * These route components can be used in the main `App.jsx`
 * to organize routes clearly based on functionality.
 */

import React from "react";
import {Route, Routes} from "react-router-dom";

import Login from "../pages/Login.jsx";
import Register from "../pages/Register.jsx";
import Main from "../pages/Main.jsx";
import UserProfile from "../components/UserProfile.jsx";
import ContactList from "../components/ContactList.jsx";


export const AuthRoutes = () => {
    return(
        <Routes>
            <Route path="/" element={<Login/>} />
            <Route path="/register" element={<Register/>} />
            <Route path="/main" element={<Main/>} />
        </Routes>
    );
};

export const ProfileRoutes = () => {
    return(
        <Routes>
            <Route path="/profile" element={<UserProfile/>} />
            <Route path="/contacts" element={<ContactList/>} />
        </Routes>
    );
};