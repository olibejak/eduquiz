import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/axios';
import type { DeckProgressSummary } from '../../types/flashcards';

type DashboardTab = 'due' | 'all' | 'my' | 'favorites';

export default function FlashcardDashboard() {
    const [dueDecks, setDueDecks] = useState<DeckProgressSummary[]>([]);
    const [tabDecks, setTabDecks] = useState<DeckProgressSummary[]>([]);
    const [activeTab, setActiveTab] = useState<DashboardTab>('due');
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchDashboardData = async () => {
            setLoading(true);
            try {
                // 1. Vždy načteme sady k opakování kvůli aktuálnímu počtu (odznáček)
                const dueRes = await api.get('/flashcards/dashboard');
                setDueDecks(dueRes.data.content || []);

                // 2. Podle aktivního tabu případně dotáhneme příslušné sady
                if (activeTab !== 'due') {
                    let endpoint = '/decks';
                    if (activeTab === 'my') endpoint = '/decks/my';
                    if (activeTab === 'favorites') endpoint = '/decks/favorites';

                    const tabRes = await api.get(endpoint, { params: { size: 100 } });
                    setTabDecks(tabRes.data.content || []);
                }
            } catch (err) {
                console.error("Chyba při načítání dat dashboardu", err);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, [activeTab]);

    // Filtrování seznamu podle hledání (aplikuje se na všechny taby kromě "due")
    const filteredDecks = tabDecks.filter(d =>
        searchQuery.trim() === ""
            ? true
            : (d.title || '').toLowerCase().includes(searchQuery.trim().toLowerCase())
    );

    const displayDecks = activeTab === 'due' ? dueDecks : filteredDecks;

    return (
        <div className="max-w-4xl mx-auto p-4 space-y-6">
            <header className="space-y-4">
                <h1 className="text-3xl font-bold" style={{ color: 'var(--text-h)' }}>Studium</h1>

                {/* Přepínač Tabů (Flex-wrap přidán pro lepší zobrazení na mobilech) */}
                <div className="flex flex-wrap gap-1 p-1 rounded-xl w-fit" style={{ backgroundColor: 'var(--accent-bg)' }}>
                    {[
                        { id: 'due', label: `K opakování (${dueDecks.length})` },
                        { id: 'all', label: 'Všechny sady' },
                        { id: 'my', label: 'Moje sady' },
                        { id: 'favorites', label: 'Oblíbené' }
                    ].map(tab => (
                        <button
                            key={tab.id}
                            onClick={() => {
                                setActiveTab(tab.id as DashboardTab);
                                setSearchQuery(''); // Reset hledání při přepnutí tabu
                            }}
                            className={`px-4 py-2 rounded-lg font-medium transition-all ${activeTab === tab.id ? 'shadow-sm' : 'opacity-60 hover:opacity-100'}`}
                            style={{
                                backgroundColor: activeTab === tab.id ? 'var(--bg)' : 'transparent',
                                color: activeTab === tab.id ? 'var(--text-h)' : 'var(--text)'
                            }}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>
            </header>

            {/* Vyhledávací input (nezobrazuje se pro "due" sady) */}
            {activeTab !== 'due' && (
                <input
                    type="text"
                    placeholder="Hledat sadu..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full p-3 rounded-lg border outline-none transition-colors"
                    style={{ backgroundColor: 'var(--bg)', borderColor: 'var(--border)', color: 'var(--text-h)' }}
                />
            )}

            {loading ? (
                <div className="text-center py-10 animate-pulse" style={{ color: 'var(--text)' }}>
                    Načítám sady...
                </div>
            ) : (
                <div className="grid gap-3">
                    {displayDecks.length === 0 ? (
                        <div className="p-10 text-center border-2 border-dashed rounded-xl" style={{ borderColor: 'var(--border)', color: 'var(--text)' }}>
                            {activeTab === 'due'
                                ? "Žádné sady k opakování. Máš hotovo!"
                                : (searchQuery ? "Hledání neodpovídá žádná sada." : "V této kategorii zatím nejsou žádné sady.")}
                        </div>
                    ) : (
                        displayDecks.map(deck => (
                            <button
                                key={deck.id}
                                onClick={() => navigate(`/flashcards/${deck.id}`, { state: { title: deck.title } })}
                                className="group p-4 border rounded-xl text-left transition-all flex justify-between items-center"
                                style={{ backgroundColor: 'var(--bg)', borderColor: 'var(--border)', boxShadow: 'var(--shadow)' }}
                            >
                                <span className="text-lg font-semibold group-hover:underline" style={{ color: 'var(--text-h)' }}>
                                    {deck.title || `Sada #${deck.id}`}
                                </span>
                                <div className="p-2 rounded-full opacity-0 group-hover:opacity-100 transition-opacity" style={{ backgroundColor: 'var(--accent-bg)' }}>
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" style={{ color: 'var(--accent)' }}>
                                        <path d="M9 18l6-6-6-6" />
                                    </svg>
                                </div>
                            </button>
                        ))
                    )}
                </div>
            )}
        </div>
    );
}