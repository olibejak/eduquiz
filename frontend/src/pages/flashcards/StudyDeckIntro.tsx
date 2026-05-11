import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { api } from '../../api/axios';
import type { DeckProgressStatus } from '../../types/flashcards';
import {useAuth} from "../../context/AuthContext.tsx";

export default function StudyDeckIntro() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const location = useLocation();

    const deckTitle = location.state?.title || `Sada #${id}`;

    const { user } = useAuth();

    const [status, setStatus] = useState<DeckProgressStatus | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchStatus = useCallback(() => {
        api.get(`/flashcards/${id}`)
            .then(res => setStatus(res.data))
            .catch(err => console.error("Chyba při načítání statusu", err))
            .finally(() => setLoading(false));
    }, [id]);

    useEffect(() => {
        fetchStatus();
    }, [fetchStatus]);

    const handleResetProgress = async () => {
        if (!window.confirm("Opravdu chcete vymazat svůj postup v této sadě? Všechny kartičky se přesunou zpět do 'Nové'.")) {
            return;
        }

        try {
            await api.delete(`/flashcards/${id}`);
            fetchStatus();
            alert("Postup byl úspěšně vymazán.");
        } catch (error) {
            console.error("Chyba při mazání postupu:", error);
            alert("Nepodařilo se vymazat postup.");
        }
    };

    if (loading || !status) return <div className="p-10 text-center" style={{ color: 'var(--text)' }}>Načítám...</div>;

    return (
        <div className="max-w-2xl mx-auto p-6 mt-10 border rounded-2xl text-center shadow-sm"
             style={{ backgroundColor: 'var(--bg)', borderColor: 'var(--border)' }}>

            <h1 className="text-3xl font-bold mb-2" style={{ color: 'var(--text-h)' }}>{deckTitle}</h1>
            <p className="mb-8" style={{ color: 'var(--text)' }}>Příprava na studium</p>

            <div className="grid grid-cols-3 gap-4 py-6 border-y mb-8" style={{ borderColor: 'var(--border)' }}>
                <div className="flex flex-col">
                    <span className="text-5xl font-bold text-blue-500">{status.newCount}</span>
                    <span className="text-sm uppercase tracking-wide font-medium mt-2" style={{ color: 'var(--text)' }}>Nové</span>
                </div>
                <div className="flex flex-col">
                    <span className="text-5xl font-bold text-red-500">{status.dueCount}</span>
                    <span className="text-sm uppercase tracking-wide font-medium mt-2" style={{ color: 'var(--text)' }}>K opakování</span>
                </div>
                <div className="flex flex-col">
                    <span className="text-5xl font-bold text-green-500">{status.learnedCount - status.dueCount}</span>
                    <span className="text-sm uppercase tracking-wide font-medium mt-2" style={{ color: 'var(--text)' }}>Naučeno</span>
                </div>
            </div>

            <div className="flex flex-col gap-3">
                <button
                    onClick={() => navigate(`/flashcards/${id}/study`)}
                    className="w-full py-4 rounded-xl font-bold text-lg transition-transform hover:-translate-y-1 shadow-md"
                    style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}
                >
                    Spustit studium
                </button>

                {/* Reset Button */}
                {user && (status.learnedCount > 0 || status.dueCount > 0) && (
                    <button
                        onClick={handleResetProgress}
                        className="w-full py-3 rounded-xl font-medium transition-colors border hover:bg-red-50"
                        style={{ borderColor: 'var(--border)', color: '#ef4444' }}
                    >
                        Vymazat postup
                    </button>
                )}
            </div>
        </div>
    );
}