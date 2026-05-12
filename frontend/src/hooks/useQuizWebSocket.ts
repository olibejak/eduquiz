import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

export const useQuizWebSocket = (pin: string | undefined, token: string) => {
    const clientRef = useRef<Client | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    const [presenceEvent, setPresenceEvent] = useState<unknown>(null);
    const [deckEvent, setDeckEvent] = useState<unknown>(null);
    const [hostEvent, setHostEvent] = useState<unknown>(null);
    const [gameStarted, setGameStarted] = useState<unknown>(null);
    const [currentQuestion, setCurrentQuestion] = useState<unknown>(null);
    const [questionResults, setQuestionResults] = useState<unknown>(null);
    const [quizFinished, setQuizFinished] = useState<boolean>(false);

    useEffect(() => {
        if (!pin) return;

        const wsUrl = import.meta.env.VITE_WS_URL;

        if (!wsUrl) {
            console.error("❌ VITE_WS_URL není definována!");
            return;
        }

        const brokerUrl = wsUrl.replace(/^http/, 'ws');

        const stompClient = new Client({
            brokerURL: brokerUrl,
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            debug: (str) => console.log('STOMP: ' + str),
            reconnectDelay: 5000,

            onConnect: () => {
                console.log('✅ STOMP Připojeno');
                setIsConnected(true);

                // User change
                stompClient.subscribe(`/topic/quiz/${pin}/presence`, (msg) => {
                    setPresenceEvent(JSON.parse(msg.body));
                });

                // Deck change
                stompClient.subscribe(`/topic/quiz/${pin}/deck`, (msg) => {
                    setDeckEvent(JSON.parse(msg.body));
                });

                // Host change
                stompClient.subscribe(`/topic/quiz/${pin}/host`, (msg) => {
                    setHostEvent(JSON.parse(msg.body));
                });

                // Quiz start
                stompClient.subscribe(`/topic/quiz/${pin}/start`, (msg) => {
                    setGameStarted(JSON.parse(msg.body));
                });

                // New question
                stompClient.subscribe(`/topic/quiz/${pin}/question`, (msg) => {
                    setCurrentQuestion(JSON.parse(msg.body));
                    setQuestionResults(null);
                });

                // End of question
                stompClient.subscribe(`/topic/quiz/${pin}/question-results`, (msg) => {
                    setQuestionResults(JSON.parse(msg.body));
                });

                // End of quiz - leaderboard
                stompClient.subscribe(`/topic/quiz/${pin}/finished`, () => {
                    setQuizFinished(true);
                });

                // Server error
                stompClient.subscribe('/user/queue/errors', (msg) => {
                    console.error("Chyba ze serveru:", msg.body);
                    alert(msg.body);
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
                console.log('❌ STOMP Odpojeno');
            },
            onStompError: (frame) => {
                console.error('❌ STOMP Error: ' + frame.headers['message']);
            },
        });

        stompClient.activate();
        clientRef.current = stompClient;

        return () => {
            stompClient.deactivate();
        };
    }, [pin]);


    const sendAnswer = (participantId: number, questionId: number, answerType: string, answerPayload: unknown) => {
        if (clientRef.current && clientRef.current.connected) {
            clientRef.current.publish({
                destination: `/app/quiz/${pin}/answer`,
                body: JSON.stringify({
                    lobbyPin: pin,
                    participantId,
                    questionId,
                    answerType,
                    payload: answerPayload
                })
            });
        }
    };

    const sendNextQuestion = () => {
        if (clientRef.current && clientRef.current.connected) {
            clientRef.current.publish({
                destination: `/app/quiz/${pin}/next`,
                body: ''
            });
        }
    };

    return {
        isConnected,
        events: {
            presenceEvent,
            deckEvent,
            hostEvent,
            gameStarted,
            currentQuestion,
            questionResults,
            quizFinished
        },
        actions: {
            sendAnswer,
            sendNextQuestion
        }
    };
};
