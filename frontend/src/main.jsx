/**
 * @file main.jsx
 * @description
 * Entry point of the React frontend application.
 */


window.global ||= window;

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
