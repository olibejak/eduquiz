import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useQuizWebSocket } from '../hooks/useQuizWebSocket';

export default function QuizLayout() {
    const { pin } = useParams<{ pin: string }>();
    const location = useLocation();
    const navigate = useNavigate();

    const { isHost, participantId, participantToken } = location.state || {};

    const [isHostPlaying, setIsHostPlaying] = useState<boolean>(true);
    const [nicknames, setNicknames] = useState<Record<number, string>>({});

    const ws = useQuizWebSocket(pin, participantToken);
    const { events } = ws;

    const [prevPresenceEvent, setPrevPresenceEvent] = useState<unknown>(null);

    if (events.presenceEvent && events.presenceEvent !== prevPresenceEvent) {
        setPrevPresenceEvent(events.presenceEvent);

        const ev = events.presenceEvent as { participantId?: number; id?: number; nickname?: string };
        const id = ev.participantId || ev.id;

        if (id && ev.nickname) {
            setNicknames(prev => ({ ...prev, [id]: ev.nickname! }));
        }
    }

    useEffect(() => {
        if (!participantToken) {
            navigate('/quiz');
        }
    }, [participantToken, navigate]);

    if (!participantToken) return null;

    return (
        <Outlet
            context={{
                ws,
                isHost,
                participantId,
                participantToken,
                isHostPlaying,
                setIsHostPlaying,
                nicknames,
                setNicknames
            }}
        />
    );
}