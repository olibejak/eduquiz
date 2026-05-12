import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/axios';
import type { DeckProgressSummary } from '../../types/flashcards';
import { useAuth } from '../../context/AuthContext';

type DashboardTab = 'due' | 'all' | 'my' | 'favorites';

export default function FlashcardDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [dueDecks, setDueDecks] = useState<DeckProgressSummary[]>([]);
    const [tabDecks, setTabDecks] = useState<DeckProgressSummary[]>([]);

    // Nepřihlášený uživatel má defaultně 'all' (Veřejné), přihlášený 'due' (K opakování)
    const [activeTab, setActiveTab] = useState<DashboardTab>(user ? 'due' : 'all');
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);

    // Pojistka: Pokud se uživatel odhlásí a zůstal by mu viset privátní tab, přepneme ho na veřejné
    useEffect(() => {
        if (!user && activeTab !== 'all') {
            setActiveTab('all');
        }
    }, [user, activeTab]);

    useEffect(() => {
        const fetchDashboardData = async () => {
            setLoading(true);
            try {
                // 1. Získáme 'due' sady (K opakování) POUZE pokud je uživatel přihlášen
                if (user) {
                    const dueRes = await api.get('/flashcards/dashboard');
                    setDueDecks(dueRes.data.content || []);
                } else {
                    setDueDecks([]);
                }

                // 2. Podle aktivního tabu dotáhneme příslušné sady
                if (activeTab !== 'due') {
                    let endpoint = '/decks';
                    if (activeTab === 'my') endpoint = '/decks/my';
                    if (activeTab === 'favorites') endpoint = '/decks/favorites';

                    // Endpoint /decks (all) nám na backendu automaticky filtruje jen PUBLIC sady
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
    }, [activeTab, user]);

    // Filtrování seznamu podle hledání (aplikuje se na všechny taby kromě "due")
    const filteredDecks = tabDecks.filter(d =>
        searchQuery.trim() === ""
            ? true
            : (d.title || '').toLowerCase().includes(searchQuery.trim().toLowerCase())
    );

    const displayDecks = activeTab === 'due' ? dueDecks : filteredDecks;

    // Dynamická definice tabů podle přihlášení
    const availableTabs = user ? [
        { id: 'due', label: `K opakování (${dueDecks.length})` },
        { id: 'all', label: 'Veřejné sady' },
        { id: 'my', label: 'Moje sady' },
        { id: 'favorites', label: 'Oblíbené' }
    ] : [
        { id: 'all', label: 'Veřejné sady' }
    ];

    return (
        <div className="max-w-4xl mx-auto p-4 space-y-6">
            <header className="space-y-4">
                <h1 className="text-3xl font-bold" style={{ color: 'var(--text-h)' }}>
                    {user ? 'Studium' : 'Knihovna veřejných sad'}
                </h1>

                {/* Přepínač Tabů */}
                <div className="flex flex-wrap gap-1 p-1 rounded-xl w-fit" style={{ backgroundColor: 'var(--accent-bg)' }}>
                    {availableTabs.map(tab => (
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