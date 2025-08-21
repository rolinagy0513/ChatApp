/**
 * @file Register.jsx
 *
 * Register component for the ChatApp frontend.
 *
 * Handles user registration by providing a form, managing input state,
 * submitting registration data to the backend API, and navigating to
 * the main app upon successful registration.
 *
 * Uses contexts for authentication state (AuthContext),
 * user profile info (ProfileContext), and feedback messaging/loading
 * state (FeedbackContext).
 *
 * Contains form input handlers, submission logic with API call,
 * and appropriate UI feedback.
 *
 * Exports: Register (React component)
 */

import React, {useContext, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';

import {AuthContext} from "../contex/AuthContext.jsx";
import {FeedbackContext} from "../contex/FeedbackContext.jsx";
import {ProfileContext} from "../contex/ProfileContext.jsx";

import apiServices from "../services/ApiServices.js";

import AuthForm from "../components/AuthForm.jsx";

import "./styles/Register.css"


const Register = () => {

    const AUTH_API_PATH = import.meta.env.VITE_API_BASE_AUTH_URL;
    const REGISTER_URL = `${AUTH_API_PATH}/register`;

    console.log(REGISTER_URL)

    const navigate = useNavigate()

    const { registerFormData, setRegisterFormData } = useContext(AuthContext);
    const { message, setMessage, isLoading, setIsLoading} = useContext(FeedbackContext);
    const {setAuthUsersId, setAuthUserName} = useContext(ProfileContext)

    const resetForm = () =>{
        setRegisterFormData({
            firstname:'',
            lastname:'',
            email: '',
            password: '',
            confirmPassword:''
        })
    }

    useEffect(()=>{
        resetForm()
    },[])

    const handleInputChange = (e) => {
        setMessage("");
        const { name, value } = e.target;
        setRegisterFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async(e)=>{

        e.preventDefault();
        setMessage('');
        setIsLoading(true)

        try{

            const response = await apiServices.post(REGISTER_URL,registerFormData)
            setMessage('Registration successful!');

            setAuthUsersId(response.user.id);
            setAuthUserName(response.user.userName);

            resetForm()

            navigate("/main");

        }catch (error){

            setMessage(error.message);
            console.error('Registration failed:', error.message);

            setRegisterFormData(prev => ({
                ...prev,
                password: '',
                confirmPassword: ''
            }));

        }finally {
            setIsLoading(false)
        }

    }

    return(
        <div className='register-page'>

            <div className='register-container'>

                <div className='text-container'>
                    <h1>ChatApp</h1>
                    <h2>Registration</h2>
                </div>

                <div className='form-container'>
                    <AuthForm
                        handleChange={handleInputChange}
                        formData={registerFormData}
                        handleSubmit={handleSubmit}
                        message={message}
                        isLoading={isLoading}
                        type={"register"}
                    />
                </div>

            </div>

        </div>

    )

}

export default Register