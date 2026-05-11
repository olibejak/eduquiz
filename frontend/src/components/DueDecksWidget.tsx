import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/axios';
import type { DeckProgressSummary } from '../types/flashcards';

export default function DueDecksWidget() {
    const [dueDecks, setDueDecks] = useState<DeckProgressSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        api.get('/flashcards/dashboard')
            .then(res => {
                setDueDecks(res.data.content || []);
            })
            .catch(err => console.error("Chyba při načítání sad k opakování:", err))
            .finally(() => setLoading(false));
    }, []);

    if (loading) {
        return (
            <div className="p-6 text-center animate-pulse border rounded-xl" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--bg)' }}>
                <span style={{ color: 'var(--text)' }}>Načítání...</span>
            </div>
        );
    }

    if (dueDecks.length === 0) {
        return (
            <div className="p-8 border-2 border-dashed rounded-xl text-center transition-all hover:bg-opacity-50"
                 style={{ borderColor: 'var(--border)', backgroundColor: 'var(--bg)' }}>
                {/* Todo: Change for svg */}
                <div className="text-4xl mb-3">🎉</div>
                <h3 className="text-lg font-bold mb-2" style={{ color: 'var(--text-h)' }}>Skvělá práce!</h3>
                <p style={{ color: 'var(--text)' }}>Pro dnešek máš všechno zopakováno.</p>
                <button
                    onClick={() => navigate('/flashcards')}
                    className="mt-4 px-4 py-2 rounded-lg font-medium text-sm transition-colors"
                    style={{ backgroundColor: 'var(--accent-bg)', color: 'var(--text-h)' }}
                >
                    Procházet ostatní sady
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <h2 className="text-xl font-bold flex items-center gap-2" style={{ color: 'var(--text-h)' }}>
                Dnes k opakování
                <span className="text-xs px-2 py-1 rounded-full bg-red-100 text-red-700">
                    {dueDecks.length}
                </span>
            </h2>

            <div className="grid gap-3">
                {dueDecks.map(deck => (
                    <button
                        key={deck.id}
                        onClick={() => navigate(`/flashcards/${deck.id}`, { state: { title: deck.title } })}
                        className="group p-4 border rounded-xl text-left transition-all hover:border-blue-400 hover:-translate-y-0.5 flex justify-between items-center"
                        style={{ backgroundColor: 'var(--bg)', borderColor: 'var(--border)', boxShadow: 'var(--shadow)' }}
                    >
                        <span className="font-semibold text-lg" style={{ color: 'var(--text-h)' }}>
                            {deck.title || `Sada #${deck.id}`}
                        </span>
                        <div className="p-2 rounded-full opacity-0 group-hover:opacity-100 transition-all bg-blue-50 text-blue-600">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                            </svg>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}