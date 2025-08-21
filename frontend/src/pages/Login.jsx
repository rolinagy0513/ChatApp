/**
 * @file Login.jsx
 *
 * Login component for the ChatApp frontend.
 *
 * Handles user authentication by providing a login form,
 * managing input state, submitting credentials to the backend API,
 * and navigating to the main app on successful login.
 *
 * Utilizes contexts to manage authentication state (AuthContext),
 * user profile data (ProfileContext), and UI feedback such as
 * error messages and loading indicators (FeedbackContext).
 *
 * Exports: Login (React component)
 */

import React, {useContext, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';

import {AuthContext} from "../contex/AuthContext.jsx";
import {FeedbackContext} from "../contex/FeedbackContext.jsx";
import {ProfileContext} from "../contex/ProfileContext.jsx";

import apiServices from "../services/ApiServices.js";

import AuthForm from "../components/AuthForm.jsx";

import "./styles/Login.css"


const Login = () => {

    const AUTH_API_PATH = import.meta.env.VITE_API_BASE_AUTH_URL;
    const LOGIN_URL = `${AUTH_API_PATH}/authenticate`;

    const navigate = useNavigate()

    const { loginFormData, setLoginFormData} = useContext(AuthContext);
    const { message, setMessage, isLoading, setIsLoading } = useContext(FeedbackContext);
    const {setAuthUsersId, setAuthUserName} = useContext(ProfileContext)

    const resetForm = () =>{
        setLoginFormData(prev => ({
            ...prev,
            password: '',
            confirmPassword: ''
        }));
    }

    useEffect(() => {
        resetForm();
    }, []);

    const handleInputChange = (e) => {
        setMessage("");
        const { name, value } = e.target;
        setLoginFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e)=>{

        e.preventDefault();
        setMessage('');
        setIsLoading(true)

        try{

            const response = await apiServices.post(LOGIN_URL,loginFormData);
            setMessage('Login successful!');

            setAuthUsersId(response.user.id);
            setAuthUserName(response.user.userName);

            resetForm()

            navigate("/main");

        }catch(error){

            setMessage(error.message);
            console.error('Registration failed:', error.message);

            setLoginFormData(prev => ({
                ...prev,
                password: '',
                confirmPassword: ''
            }));

        }finally {
            setIsLoading(false)
        }

    }

    return(
        <div className='login-page'>

            <div className='login-container'>

                <div className='text-container'>
                    <h1>ChatApp</h1>
                    <h2>Welcome back!</h2>
                </div>

                <div className='form-container'>
                    <AuthForm
                        handleChange={handleInputChange}
                        formData={loginFormData}
                        handleSubmit={handleSubmit}
                        message={message}
                        isLoading={isLoading}
                        type={"login"}
                    />
                </div>

            </div>

        </div>

    )

};

export default Login;