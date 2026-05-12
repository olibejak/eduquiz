import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import type { DeckSummary } from '../types/deck';
import { DECK_TAGS } from '../types/deck';

export type VisibilityType = 'PUBLIC' | 'PRIVATE';
export type QuestionType = 'STANDARD' | 'MULTIPLE_CHOICE' | 'MATCHING';
export type AnswerType = 'STANDARD' | 'CHOICE' | 'MATCHING';

export interface AnswerPayload {
    type: 'CHOICE' | 'MATCHING';
    isCorrect?: boolean; // Pro CHOICE
    associate?: boolean; // Pro MATCHING
    matchId?: number;    // Pro MATCHING
}

export interface AnswerResponseDTO {
    id: number;
    text: string;
    type: AnswerType;
    payload: AnswerPayload;
}

export interface QuestionResponseDTO {
    id: number;
    text: string;
    questionType: QuestionType;
    answers: AnswerResponseDTO[];
    duration: number;
}

export interface DeckDetailsResponseDTO {
    id: number;
    title: string;
    description: string;
    visibility: VisibilityType;
    authorId: string;
    tags: string[];
    favoritesCount: number;
    questions: QuestionResponseDTO[];
    createdAt: string;
    updatedAt: string;
}

export default function Library() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [decks, setDecks] = useState<DeckSummary[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [favoriteIds, setFavoriteIds] = useState<Set<number>>(new Set());

    const [activeTab, setActiveTab] = useState<'all' | 'my' | 'favorites'>('all');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [selectedTags, setSelectedTags] = useState<string[]>([]);

    const [isPreviewOpen, setIsPreviewOpen] = useState(false);
    const [previewLoading, setPreviewLoading] = useState(false);
    const [selectedDeckDetails, setSelectedDeckDetails] = useState<DeckDetailsResponseDTO | null>(null);

    const isAdmin = user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN';

    useEffect(() => {
        if (!user) return;
        api.get('/decks/favorites', { params: { size: 1000 } })
            .then(res => {
                const ids = res.data.content.map((d: DeckSummary) => d.id);
                setFavoriteIds(new Set(ids));
            })
            .catch(err => console.error("Nepodařilo se načíst oblíbené ID", err));
    }, [user]);

    const fetchDecks = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            let endpoint = '/decks';
            const params: Record<string, string | number> = { page: 0, size: 20 };

            if (activeTab === 'all') {
                if (selectedTags.length > 0) {
                    endpoint = "/decks/search";
                    params.tags = selectedTags.join(',');
                } else if (searchKeyword) {
                    endpoint = "/decks/search";
                    params.keyword = searchKeyword;
                }
            } else if (activeTab === 'my' && user) {
                endpoint = '/decks/my';
            } else if (activeTab === 'favorites') {
                endpoint = '/decks/favorites';
            }

            const response = await api.get(endpoint, { params });
            setDecks(response.data.content || []);
        } catch (err) {
            console.error("Chyba při načítání sad:", err);
            setError("Nepodařilo se načíst sady otázek.");
        } finally {
            setLoading(false);
        }
    }, [activeTab, searchKeyword, selectedTags, user]);

    const handleDeleteDeck = async (deckId: number) => {
        if (!window.confirm("Opravdu chcete smazat tuto sadu? Tato akce je nevratná a smaže i všechny její otázky.")) return;
        try {
            await api.delete(`/decks/${deckId}`);
            setDecks(prevDecks => prevDecks.filter(deck => deck.id !== deckId));
        } catch (err) {
            console.error("Chyba při mazání sady:", err);
            alert("Sadu se nepodařilo smazat. Zkuste to prosím znovu.");
        }
    };

    const handleAdminDelete = async (deckId: number) => {
        if (!window.confirm("Jako administrátor se chystáte smazat cizí sadu. Tato akce je nevratná. Pokračovat?")) return;
        try {
            await api.delete(`/decks/${deckId}`);
            setDecks(prevDecks => prevDecks.filter(deck => deck.id !== deckId));
            setIsPreviewOpen(false); // Zavřít modal po úspěšném smazání
            alert("Sada byla úspěšně smazána administrátorem.");
        } catch (err) {
            console.error("Chyba při mazání adminem:", err);
            alert("Nepodařilo se smazat sadu.");
        }
    };

    const handleToggleFavorite = async (deckId: number) => {
        if (!user) {
            alert("Pro přidání do oblíbených se musíte přihlásit.");
            return;
        }
        const isFav = favoriteIds.has(deckId);

        setFavoriteIds(prev => {
            const next = new Set(prev);
            if (isFav) next.delete(deckId);
            else next.add(deckId);
            return next;
        });

        setDecks(prevDecks => {
            if (activeTab === 'favorites' && isFav) return prevDecks.filter(d => d.id !== deckId);
            return prevDecks.map(d => {
                if (d.id === deckId) return { ...d, favoritesCount: isFav ? d.favoritesCount - 1 : d.favoritesCount + 1 };
                return d;
            });
        });

        try {
            await api.post(`/decks/${deckId}/favorite`);
        } catch (err) {
            console.error("Chyba při úpravě oblíbených:", err);
            alert("Nepodařilo se aktualizovat oblíbené.");
            fetchDecks();
        }
    };

    const handleOpenPreview = async (deckId: number) => {
        setIsPreviewOpen(true);
        setPreviewLoading(true);
        try {
            const response = await api.get(`/decks/${deckId}`);
            setSelectedDeckDetails(response.data);
        } catch (err) {
            console.error("Chyba při načítání detailů sady:", err);
            alert("Nepodařilo se načíst detaily sady.");
            setIsPreviewOpen(false);
        } finally {
            setPreviewLoading(false);
        }
    };

    const toggleTag = (tagId: string) => {
        setSelectedTags(prev => prev.includes(tagId) ? prev.filter(t => t !== tagId) : [...prev, tagId]);
    };

    useEffect(() => {
        const timer = window.setTimeout(() => { void fetchDecks(); }, 0);
        return () => window.clearTimeout(timer);
    }, [fetchDecks]);

    return (
        <div className="max-w-7xl mx-auto p-4 space-y-6">
            <h1 className="text-3xl font-bold" style={{ color: 'var(--text-h)' }}>Knihovna sad</h1>

            {/* Navigation */}
            <div className="flex space-x-4 border-b pb-2" style={{ borderColor: 'var(--border)' }}>
                <button
                    className="font-semibold pb-1 transition-colors"
                    style={{
                        color: activeTab === 'all' ? 'var(--text-h)' : 'var(--text)',
                        borderBottom: activeTab === 'all' ? '2px solid var(--text-h)' : '2px solid transparent'
                    }}
                    onClick={() => { setActiveTab('all'); setSelectedTags([]); setSearchKeyword(''); }}
                >
                    Všechny sady
                </button>
                {user && (
                    <>
                        <button
                            className="font-semibold pb-1 transition-colors"
                            style={{
                                color: activeTab === 'my' ? 'var(--text-h)' : 'var(--text)',
                                borderBottom: activeTab === 'my' ? '2px solid var(--text-h)' : '2px solid transparent'
                            }}
                            onClick={() => setActiveTab('my')}
                        >
                            Moje sady
                        </button>
                        <button
                            className="font-semibold pb-1 transition-colors"
                            style={{
                                color: activeTab === 'favorites' ? 'var(--text-h)' : 'var(--text)',
                                borderBottom: activeTab === 'favorites' ? '2px solid var(--text-h)' : '2px solid transparent'
                            }}
                            onClick={() => setActiveTab('favorites')}
                        >
                            Oblíbené
                        </button>
                    </>
                )}
            </div>

            {/* Tags */}
            {activeTab === 'all' && (
                <div className="space-y-4">
                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Hledat podle názvu... (vymažte tagy pro textové vyhledávání)"
                            className="w-full p-2 pr-10 rounded-md outline-none transition-colors"
                            style={{ backgroundColor: 'transparent', color: 'var(--text)', border: '1px solid var(--border)' }}
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            disabled={selectedTags.length > 0}
                        />
                        <div className="absolute right-3 top-1/2 -translate-y-1/2 group flex items-center">
                            <div className="cursor-help flex items-center justify-center w-5 h-5 rounded-full border text-xs font-bold transition-opacity hover:opacity-70"
                                 style={{ color: 'var(--text)', borderColor: 'var(--border)' }}>!</div>
                            <div className="absolute bottom-full -right-2 mb-2 w-64 p-2.5 text-xs text-center rounded shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 pointer-events-none"
                                 style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}>
                                V současné chvíli není funkční souběžné vyhledávání pomocí názvu a štítků.
                                <div className="absolute top-full right-3 border-4 border-transparent" style={{ borderTopColor: 'var(--accent)' }}></div>
                            </div>
                        </div>
                    </div>
                    <div className="flex flex-wrap gap-2">
                        {DECK_TAGS.map(tag => (
                            <button
                                key={tag.id}
                                onClick={() => toggleTag(tag.id)}
                                className="px-3 py-1 rounded-full text-sm transition-colors border"
                                style={{
                                    backgroundColor: selectedTags.includes(tag.id) ? 'var(--accent)' : 'var(--accent-bg)',
                                    color: selectedTags.includes(tag.id) ? 'var(--bg)' : 'var(--text)',
                                    borderColor: 'var(--accent-border)'
                                }}
                            >
                                {tag.label}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            {loading && <p style={{ color: 'var(--text)' }}>Načítám sady...</p>}
            {error && <p style={{ color: '#ef4444' }}>{error}</p>}

            {/* Deck grid */}
            {!loading && !error && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {decks.length === 0 ? (
                        <p className="col-span-full" style={{ color: 'var(--text)' }}>Zatím tu nejsou žádné sady.</p>
                    ) : (
                        decks.map(deck => {
                            return (
                                <div key={deck.id}
                                     onClick={() => handleOpenPreview(deck.id)}
                                     className="border p-4 rounded-lg flex flex-col transition-all duration-200 relative group cursor-pointer hover:shadow-md"
                                     style={{ backgroundColor: 'var(--bg)', borderColor: 'var(--border)', boxShadow: 'var(--shadow)' }}>

                                    <div className="flex justify-between items-start mb-2">
                                        <h2 className="text-xl font-bold truncate pr-2 group-hover:underline" style={{ color: 'var(--text-h)' }}>{deck.title}</h2>

                                        <div className="flex items-center gap-1">
                                            <span className="text-xs px-2 py-1 rounded whitespace-nowrap"
                                                  style={{ backgroundColor: 'var(--code-bg)', color: 'var(--text)', border: '1px solid var(--border)' }}>
                                                {deck.numberOfQuestions} otázek
                                            </span>

                                            {activeTab === 'my' && (
                                                <>
                                                    <button
                                                        onClick={(e) => { e.stopPropagation(); navigate(`/edit/${deck.id}`); }}
                                                        className="p-1 rounded-md hover:bg-blue-50 transition-colors opacity-70 hover:opacity-100 ml-1"
                                                        title="Upravit sadu"
                                                    >
                                                        <span style={{ color: '#3b82f6' }}>✎</span>
                                                    </button>
                                                    <button
                                                        onClick={(e) => { e.stopPropagation(); handleDeleteDeck(deck.id); }}
                                                        className="p-1 rounded-md hover:bg-red-50 transition-colors opacity-70 hover:opacity-100"
                                                        title="Smazat sadu"
                                                    >
                                                        <span style={{ color: '#ef4444' }}>✕</span>
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </div>

                                    <p className="text-sm grow line-clamp-2" style={{ color: 'var(--text)' }}>{deck.description}</p>

                                    {deck.tags && deck.tags.length > 0 && (
                                        <div className="flex flex-wrap gap-1 mt-3">
                                            {deck.tags.map(tagId => {
                                                const tagInfo = DECK_TAGS.find(t => t.id === tagId);
                                                return (
                                                    <span key={tagId} className="text-xs px-2 py-0.5 rounded border"
                                                          style={{ backgroundColor: 'var(--accent-bg)', color: 'var(--text)', borderColor: 'var(--accent-border)' }}>
                                                        {tagInfo ? tagInfo.label : tagId}
                                                    </span>
                                                );
                                            })}
                                        </div>
                                    )}

                                    <div className="mt-4 flex justify-between items-center text-sm border-t pt-3" style={{ borderColor: 'var(--border)', color: 'var(--text)' }}>
                                        <span className="truncate pr-2">Autor: {deck.authorName}</span>

                                        <button
                                            onClick={(e) => { e.stopPropagation(); handleToggleFavorite(deck.id); }}
                                            className={`flex items-center space-x-1 p-1 rounded-md transition-transform ${user ? 'hover:scale-110 cursor-pointer' : 'cursor-default'}`}
                                        >
                                            <span style={{ color: favoriteIds.has(deck.id) ? '#ef4444' : 'var(--text)', opacity: favoriteIds.has(deck.id) ? 1 : 0.5 }}>
                                                {favoriteIds.has(deck.id) ? '❤️' : '🤍'}
                                            </span>
                                            <span style={{ color: 'var(--text-h)' }}>{deck.favoritesCount}</span>
                                        </button>
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            )}

            {/* Modal window */}
            {isPreviewOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 p-4 backdrop-blur-sm" onClick={() => setIsPreviewOpen(false)}>
                    <div
                        className="w-full max-w-3xl flex flex-col shadow-2xl overflow-hidden rounded-2xl"
                        style={{ backgroundColor: 'var(--bg)', border: '1px solid var(--border)', maxHeight: '90vh' }}
                        onClick={e => e.stopPropagation()} // Zabránění zavření při kliknutí dovnitř
                    >
                        {/* Modal header */}
                        <div className="p-5 border-b flex justify-between items-center" style={{ borderColor: 'var(--border)' }}>
                            <h2 className="text-2xl font-bold truncate pr-4" style={{ color: 'var(--text-h)' }}>
                                {previewLoading ? 'Načítání podrobností...' : selectedDeckDetails?.title}
                            </h2>
                            <div className="flex items-center gap-4 shrink-0">
                                {/* Admin DELETE */}
                                {isAdmin && selectedDeckDetails && (
                                    <button
                                        onClick={() => handleAdminDelete(selectedDeckDetails.id)}
                                        className="px-4 py-1.5 bg-red-100 text-red-600 border border-red-200 rounded-lg hover:bg-red-200 font-bold text-sm transition-colors"
                                        title="Natvrdo smazat tuto sadu z databáze"
                                    >
                                        Smazat (Admin)
                                    </button>
                                )}
                                <button onClick={() => setIsPreviewOpen(false)} className="text-3xl leading-none hover:opacity-70 transition-opacity" style={{ color: 'var(--text)' }}>
                                    &times;
                                </button>
                            </div>
                        </div>

                        {/* Modal body */}
                        <div className="p-6 overflow-y-auto custom-scrollbar flex-1 space-y-6">
                            {previewLoading ? (
                                <div className="text-center py-10 animate-pulse font-medium" style={{ color: 'var(--text)' }}>
                                    Stahuji otázky a odpovědi ze serveru...
                                </div>
                            ) : selectedDeckDetails ? (
                                <>
                                    <p className="text-lg whitespace-pre-wrap" style={{ color: 'var(--text)' }}>
                                        {selectedDeckDetails.description || 'Bez popisu.'}
                                    </p>

                                    {/* Questions */}
                                    <h3 className="text-xl font-bold mt-8 mb-4 border-b pb-2" style={{ color: 'var(--text-h)', borderColor: 'var(--border)' }}>
                                        Otázky v sadě ({selectedDeckDetails.questions?.length || 0})
                                    </h3>

                                    <div className="space-y-4">
                                        {selectedDeckDetails.questions?.length === 0 ? (
                                            <p className="italic text-center" style={{ color: 'var(--text)' }}>Tato sada je zatím prázdná.</p>
                                        ) : (
                                            selectedDeckDetails.questions?.map((q, index) => (
                                                <div key={q.id} className="p-4 border rounded-xl" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--accent-bg)' }}>
                                                    <div className="font-bold text-lg mb-3" style={{ color: 'var(--text-h)' }}>
                                                        {index + 1}. {q.text}
                                                    </div>

                                                    <div className="space-y-1.5 pl-2 border-l-2" style={{ borderColor: 'var(--border)' }}>
                                                        {q.answers?.map(a => {
                                                            // Rozpoznání správné odpovědi podle typu otázky
                                                            const isCorrectChoice = a.payload?.type === 'CHOICE' && a.payload.isCorrect;
                                                            const isLeftMatching = a.payload?.type === 'MATCHING' && !a.payload.associate;

                                                            return (
                                                                <div key={a.id} className="flex items-center gap-2 text-sm">
                                                                    {isCorrectChoice ? (
                                                                        <span style={{ color: '#10b981', fontWeight: 'bold' }}>✓ {a.text}</span>
                                                                    ) : isLeftMatching ? (
                                                                        <span style={{ color: 'var(--accent)', fontWeight: '600' }}>• {a.text}</span>
                                                                    ) : (
                                                                        <span style={{ color: 'var(--text)' }}>○ {a.text}</span>
                                                                    )}
                                                                </div>
                                                            );
                                                        })}
                                                    </div>
                                                </div>
                                            ))
                                        )}
                                    </div>
                                </>
                            ) : (
                                <p className="text-center" style={{ color: 'var(--text)' }}>Nepodařilo se načíst data.</p>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}