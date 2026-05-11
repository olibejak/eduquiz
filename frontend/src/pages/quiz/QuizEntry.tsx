import { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/axios';
import { useAuth } from '../../context/AuthContext';

const generateUUID = () => {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID();
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

export default function QuizEntry() {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [pin, setPin] = useState('');
    const [nickname, setNickname] = useState(user ? user.username : '');
    const [isCreating, setIsCreating] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!localStorage.getItem('quiz_device_id')) {
            localStorage.setItem('quiz_device_id', generateUUID());
        }
    }, []);

    const handleAction = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!nickname.trim()) {
            setError('Přezdívka nesmí být prázdná.');
            return;
        }

        if (!isCreating && !pin.trim()) {
            setError('Zadejte PIN kód lobby.');
            return;
        }

        setLoading(true);

        const deviceId = localStorage.getItem('quiz_device_id');
        const userId = user?.id || null;

        const payload = { userId, nickname: nickname.trim(), deviceId };

        try {
            if (isCreating) {
                const res = await api.post('/quiz/create', payload);
                const { lobbyPin, hostToken, hostParticipantId } = res.data;

                navigate(`/quiz/${lobbyPin}/lobby`, {
                    state: { isHost: true, participantId: hostParticipantId, participantToken: hostToken }
                });
            } else {
                const res = await api.post(`/quiz/${pin.trim()}/join`, payload);
                const { token, participantId } = res.data;

                navigate(`/quiz/${pin.trim()}/lobby`, {
                    state: { isHost: false, participantToken: token, participantId: participantId }
                });
            }
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 404) setError('Lobby s tímto PINem neexistuje.');
                else if (err.response?.status === 400) setError('Neplatná přezdívka nebo PIN.');
                else setError('Nastala chyba při komunikaci se serverem.');
            } else if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('Nastala chyba při komunikaci se serverem.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[80vh] px-4 py-8">
            <div className="w-full max-w-md p-6 sm:p-8 rounded-3xl shadow-2xl border transition-all bg-[var(--bg)] border-[var(--border)] text-center">

                <div className="mb-8">
                    <h1 className="text-3xl sm:text-4xl font-black tracking-tight mb-2 text-[var(--text-h)]">
                        EduQuiz
                    </h1>
                    <p className="font-medium opacity-70 text-[var(--text)]">
                        {isCreating ? 'Založte novou hru jako hostitel' : 'Připojte se do existující hry'}
                    </p>
                </div>

                <form onSubmit={handleAction} className="space-y-4">
                    {!isCreating && (
                        <div>
                            <input
                                type="text"
                                placeholder="PIN hry"
                                value={pin}
                                onChange={(e) => setPin(e.target.value.toUpperCase())}
                                maxLength={10}
                                className="w-full text-center text-2xl sm:text-3xl font-bold tracking-widest p-4 rounded-xl border-2 outline-none transition-colors bg-[var(--accent-bg)] border-[var(--border)] text-[var(--text-h)] focus:border-[var(--accent)]"
                            />
                        </div>
                    )}

                    <div>
                        <input
                            type="text"
                            placeholder="Tvoje přezdívka"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            maxLength={15}
                            className="w-full text-center text-lg sm:text-xl font-medium p-4 rounded-xl border-2 outline-none transition-colors bg-[var(--accent-bg)] border-[var(--border)] text-[var(--text-h)] focus:border-[var(--accent)]"
                        />
                    </div>

                    {error && (
                        <div className="text-red-500 font-semibold text-sm p-3 bg-red-500/10 rounded-xl border border-red-500/20">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full py-4 text-lg sm:text-xl font-bold rounded-xl transition-transform active:scale-95 shadow-md disabled:opacity-70 bg-[var(--accent)] text-[var(--bg)]"
                    >
                        {loading ? 'Načítání...' : (isCreating ? 'Vytvořit hru' : 'Vstoupit do hry')}
                    </button>
                </form>

                <div className="mt-8 pt-6 border-t border-[var(--border)]">
                    <p className="text-sm text-[var(--text)]">
                        {isCreating ? 'Chcete se připojit k existující hře?' : 'Chcete založit novou hru?'}
                    </p>
                    <button
                        type="button"
                        onClick={() => {
                            setIsCreating(!isCreating);
                            setError(null);
                        }}
                        className="mt-2 font-bold hover:underline text-[var(--text-h)]"
                    >
                        {isCreating ? 'Připojit se pomocí PINu' : 'Založit novou hru'}
                    </button>
                </div>
            </div>
        </div>
    );
}