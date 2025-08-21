/**
 * @file WebsocketServices.js
 *
 * A singleton WebSocket service that wraps SockJS and STOMP to manage real-time
 * communication with a backend server. Provides methods for connecting, subscribing,
 * sending messages, and handling connection lifecycle.
 */

import SockJS from 'sockjs-client';

import { Client } from '@stomp/stompjs';

const websocketServices = {

    client: null,
    isConnected: false,

    /**
     * Establishes a WebSocket connection using SockJS and STOMP.
     *
     * @param {string} sockUrl - The URL of the WebSocket endpoint.
     * @param {Object} options - Optional lifecycle and configuration callbacks.
     * @param {Function} [options.onConnect] - Called when connection is successfully established.
     * @param {Function} [options.onDisconnect] - Called when the connection is closed.
     * @param {Function} [options.onError] - Called on STOMP errors.
     * @param {number} [options.reconnectDelay=5000] - Delay before reconnect attempts.
     * @param {number} [options.heartbeatIncoming=4000] - Interval for incoming heartbeats.
     * @param {number} [options.heartbeatOutgoing=4000] - Interval for outgoing heartbeats.
     *
     * @returns {Client} The STOMP client instance.
     */

    connect: (sockUrl, options = {}) => {

        const {
            onConnect = null,
            onDisconnect = null,
            onError = null,
            reconnectDelay = 5000,
            heartbeatIncoming = 4000,
            heartbeatOutgoing = 4000
        } = options;

        const socket = new SockJS(sockUrl);
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay,
            heartbeatIncoming,
            heartbeatOutgoing,

            onConnect: (frame) => {
                websocketServices.isConnected = true;

                if (onConnect && typeof onConnect === 'function') {
                    onConnect(client, frame);
                }
            },

            onDisconnect: () => {

                websocketServices.isConnected = false;

                if (onDisconnect && typeof onDisconnect === 'function') {
                    onDisconnect();
                }
            },

            onStompError: (frame) => {
                console.error('❌ STOMP Error:', frame.headers['message']);
                console.error('Details:', frame.body);
                websocketServices.isConnected = false;

                if (onError && typeof onError === 'function') {
                    onError(frame);
                }
            }
        });

        websocketServices.client = client;
        client.activate();

        return client;

   },

    /**
     * Sends a message to a specified WebSocket destination.
     *
     * @param {string} destination - STOMP destination (e.g., `/app/sendMessage`).
     * @param {Object} messageBody - JSON-serializable message payload.
     *
     * @returns {boolean} True if message was sent, false otherwise.
     */

    sendMessage: (destination, messageBody) => {
        if (!websocketServices.client || !websocketServices.isConnected) {
            console.error('❌ Cannot send message - WebSocket not connected');
            return false;
        }

        try {
            websocketServices.client.publish({
                destination: destination,
                body: JSON.stringify(messageBody)
            });

            return true;
        } catch (error) {
            console.error('❌ Error sending message:', error);
            return false;
        }
    },

    /**
     * Subscribes to a WebSocket topic and handles incoming messages.
     *
     * @param {string} destination - Topic to subscribe to (e.g., `/user/queue/messages`).
     * @param {Function} messageHandler - Callback to handle parsed message payload.
     *
     * @returns {StompSubscription|null} A subscription object, or null if not connected.
     */

    subscribe: (destination, messageHandler) => {
        if (!websocketServices.client || !websocketServices.isConnected) {
            console.error('❌ Cannot subscribe - WebSocket not connected');
            return null;
        }

        console.log('📧 Subscribing to:', destination);
        return websocketServices.client.subscribe(destination, message => {
            try {
                const parsedMessage = JSON.parse(message.body);
                if (messageHandler && typeof messageHandler === 'function') {
                    messageHandler(parsedMessage);
                }
            } catch (error) {
                console.error('Error parsing subscription message:', error);
            }
        });
    },

    /**
     * Gracefully disconnects the WebSocket connection and resets state.
     */

    disconnect: () => {
        if (websocketServices.client && websocketServices.client.active) {
            websocketServices.client.deactivate();
            websocketServices.client = null;
            websocketServices.isConnected = false;
        }
    },

    /**
     * Returns current connection status of the WebSocket.
     *
     * @returns {Object} An object with connection info:
     *   - isConnected: boolean
     *   - hasClient: boolean
     *   - clientActive: boolean
     */

    getConnectionStatus: () => {
        return {
            isConnected: websocketServices.isConnected,
            hasClient: websocketServices.client !== null,
            clientActive: websocketServices.client?.active || false
        };
    }
};

export default websocketServices;