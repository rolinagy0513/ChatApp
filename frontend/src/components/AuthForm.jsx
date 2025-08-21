/**
 * @file AuthForm.jsx
 * @description
 * Reusable form component used for both user login and registration flows.
 *
 * This form adapts based on the `type` prop and supports real-time form state management
 * via props and context-based feedback. It includes client-side password visibility toggle
 * and conditional field rendering based on the authentication mode.
 *
 * ### Responsibilities:
 * - Renders a dynamic form for login or registration, based on the `type` prop.
 * - Displays backend or context-driven feedback messages.
 * - Manages password visibility toggle via checkbox.
 * - Triggers navigation to registration or login page on link click.
 *
 * ### Props:
 * - `formData` (object): Current state of form inputs.
 * - `handleChange` (function): Input change handler for controlled fields.
 * - `handleSubmit` (function): Submission handler triggered on form submission.
 * - `isLoading` (boolean): Indicates loading state for disabling/enabling submit.
 * - `type` (string): Form mode — `"login"` or `"register"` — determines rendered inputs.
 *
 * ### Contexts Used:
 * - **FeedbackContext**: Displays user feedback messages from authentication actions.
 *
 * ### Dependencies:
 * - Uses `useNavigate` from `react-router-dom` for route redirection.
 * - Controlled components pattern used for input fields.
 * - Dynamic rendering of password and confirmation fields depending on form mode.
 */


import React,{ useState, useContext } from 'react';
import {useNavigate} from "react-router-dom";

import {FeedbackContext} from "../contex/FeedbackContext.jsx";

import "./styles/AuthForm.css";

function AuthForm({ handleChange, formData, handleSubmit, isLoading, type}) {

    const [showPassword, setShowPassword] = useState(false);

    const navigate = useNavigate();

    const {message} = useContext(FeedbackContext)

    const redirectToRegister = () =>{
        navigate("/register");
    }

    const redirectToLogin = () =>{
        navigate("/");
    }
    return (
        <div className='auth-form-container'>
            <form className='auth-form-element' onSubmit={handleSubmit}>

                <div className='feedback-message'>
                    {message && <div>{message}</div>}
                </div>


                {(type === "register") && (

                    <div>
                        <input
                        type = "text"
                        name = "firstname"
                        value={formData.firstname}
                        onChange={handleChange}
                        required
                        placeholder="First name"
                        />
                    </div>

                )}

                {(type === "register") && (

                    <div>
                        <input
                            type = "text"
                            name = "lastname"
                            value={formData.lastname}
                            onChange={handleChange}
                            required
                            placeholder="Last name"
                        />
                    </div>

                )}

                <div>
                    <input
                        type="text"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                        placeholder="Email"
                    />
                </div>

                <div>
                    <input
                        type={ showPassword ? 'text' : 'password' }
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                        placeholder="Password"
                    />
                </div>

                {(type === "register") && (

                    <div>
                        <input
                            type = { showPassword ? 'text' : 'password' }
                            name = "confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                            placeholder="Confirm Password"
                        />
                    </div>

                )}

                <div>
                    <label className='auth-form-checkbox'>
                        <input
                            type="checkbox"
                            name='checkbox'
                            checked={showPassword}
                            onChange={() => setShowPassword(!showPassword)}
                        />
                        Show Password
                    </label>
                </div>

                {type === "login" &&(
                    <div className='auth-form-text'>
                        <p onClick={redirectToRegister}>Don't have an account Register one here!</p>
                    </div>
                )}

                {type === "register" &&(
                    <div className='auth-form-text'>
                        <p onClick={redirectToLogin}>Already have an account ? Log in here!</p>
                    </div>
                )}

                <button type={"submit"}>{isLoading? 'loading...' : 'Submit'}</button>

            </form>





        </div>
    );
}

export default AuthForm;
