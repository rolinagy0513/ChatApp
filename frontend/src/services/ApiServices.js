/**
 * @file ApiServices.js
 *
 * Provides utility functions to perform HTTP requests (`GET`, `POST`, and `DELETE`)
 * using the Fetch API with JSON payloads and built-in error handling.
 *
 * All requests include credentials (`cookies`, `auth tokens`) and use JSON headers by default.
 *
 * Each method:
 * - Automatically handles `Content-Type: application/json`
 * - Parses responses as JSON (or plain text if JSON parsing fails)
 * - Throws a descriptive error when the response is not OK (non-2xx status)
 */


const apiServices = {

    /**
     * Sends a GET request to the given URL.
     *
     * @param {string} url - The endpoint URL to fetch.
     * @returns {Promise<Object>} - Parsed JSON response.
     * @throws {Error} - If the request fails or returns a non-OK status.
     */

    get: async (url) => {
        try{

            const headers = {
                "Content-Type":"application/json"
            }

            const response = await fetch(url, {
                method: "GET",
                headers: headers,
                credentials: 'include'
            });

            const data = await response.json();


            if (!response.ok) {
                throw new Error(data.message);
            }

            return data;

        }catch (error){

            console.error("Api error: " + error.message)
            throw error;

        }

    },

    /**
     * Sends a POST request to the provided URL with optional JSON data.
     *
     * @param {string} url - The endpoint to send the POST request to.
     * @param {Object} [data=null] - Optional data object to be sent as JSON in the request body.
     * @returns {Promise<Object>} - Parsed JSON response from the server.
     * @throws {Error} - Throws an error if the request fails or returns a non-OK response.
     */

    post: async (url, data = null) =>{

        try{

            const headers = {
                "Content-Type":"application/json"
            }

            const body = JSON.stringify(data)

            const response = await fetch(url, {
                method: "POST",
                headers: headers,
                credentials: 'include',
                body: body
            });

            const responseData = await response.json();

            if (!response.ok) {
                throw new Error(responseData.message || "Request failed");
            }

            return responseData;

        }catch(error){

            console.error("Api error: " + error.message)
            throw error;

        }
    },

    /**
     * Sends a DELETE request to the provided URL.
     *
     * @param {string} url - The endpoint to send the DELETE request to.
     * @returns {Promise<Object|string>} - Parsed JSON response or plain text from the server.
     * @throws {Error} - Throws an error if the request fails or returns a non-OK response.
     */

    delete: async (url) => {
        try {
            const headers = {
                "Content-Type": "application/json"
            };
            const response = await fetch(url, {
                method: "DELETE",
                headers: headers,
                credentials: 'include'
            });

            let responseData;
            try {
                responseData = await response.json();
            } catch (jsonError) {
                responseData = await response.text();
            }

            if (!response.ok) {
                throw new Error(typeof responseData === 'object'
                    ? responseData.message
                    : responseData
                );
            }

            return responseData;
        } catch (error) {
            console.error("Api error: " + error.message);
            throw error;
        }
    }

}

export default apiServices;