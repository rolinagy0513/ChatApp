/**
 * @file App.jsx
 * @description
 * Root component of the React application.
 * This file sets up global routing, wraps the app in various context providers,
 * and integrates route configurations defined in `./routes/RoutesConfig.jsx`
 * through `AuthRoutes` and `ProfileRoutes`.
 *
 * Routing is managed using `react-router-dom`.
 *
 * All components rendered through the route structure have access to shared state
 * and functionality via the following context providers:
 *
 * - FeedBackProvider
 * - AuthProvider
 * - ProfileProvider
 * - FriendProvider
 * - AlertProvider
 * - ModalProvider
 * - ChatProvider
 */

import {BrowserRouter as Router} from 'react-router-dom';

import {AuthRoutes, ProfileRoutes} from "./routes/RoutesConfig.jsx";

import {AuthProvider} from "./contex/AuthContext.jsx";
import {FeedBackProvider} from "./contex/FeedbackContext.jsx";
import {ProfileProvider} from "./contex/ProfileContext.jsx";
import {FriendProvider} from "./contex/FriendContext.jsx";
import {AlertProvider} from "./contex/AlertContext.jsx";
import {ModalProvider} from "./contex/ModalContext.jsx";
import {ChatProvider} from "./contex/ChatContext.jsx";

function App() {

    return (
        <Router>
                <FeedBackProvider>
                    <AuthProvider>
                        <ProfileProvider>
                            <FriendProvider>
                                <AlertProvider>
                                    <ModalProvider>
                                        <ChatProvider>
                                            <AuthRoutes />
                                            <ProfileRoutes />
                                        </ChatProvider>
                                    </ModalProvider>
                                </AlertProvider>
                            </FriendProvider>
                        </ProfileProvider>
                    </AuthProvider>
                </FeedBackProvider>
        </Router>
    );
}

export default App
