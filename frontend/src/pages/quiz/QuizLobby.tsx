import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/axios';
import { useQuizContext } from '../../hooks/useQuizContext';

type LobbyDeck = { deckId: number; name: string };
type LobbyParticipant = { id: number; nickname: string; role: string; isConnected: boolean };
type LobbyData = {
    pin: string;
    currentState: string;
    participants: LobbyParticipant[];
    decks: LobbyDeck[];
} | null;

type SearchResultDeck = {
    id: number;
    title: string;
    authorName: string;
    numberOfQuestions: number;
};

type RawParticipantLike = {
    id?: unknown;
    participantId?: unknown;
    nickname?: unknown;
    role?: unknown;
    isConnected?: unknown;
};

type RawDeckLike = {
    deckId?: unknown;
    id?: unknown;
    title?: unknown;
};

const createQuizApiLocal = (participantToken: string | undefined) => axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
        'Quiz-Token': participantToken,
        'ngrok-skip-browser-warning': 'true',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache',
        'Expires': '0'
    }
});

export default function QuizLobby() {
    const { pin } = useParams<{ pin: string }>();
    const navigate = useNavigate();

    const { ws, isHost, participantId, participantToken, isHostPlaying, setIsHostPlaying } = useQuizContext();
    const { isConnected, events } = ws;

    useEffect(() => {
        if (!participantToken) {
            navigate('/quiz');
        }
    }, [participantToken, navigate]);

    const [lobbyData, setLobbyData] = useState<LobbyData>(null);

    // STAVY PRO UI
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false); // NOVÉ: Stav pro mobilní menu

    const [searchTab, setSearchTab] = useState<'all' | 'my' | 'favorites'>('all');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState<SearchResultDeck[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    // -------------
    // INITIAL LOAD
    // -------------
    useEffect(() => {
        const fetchLobby = async () => {
            if (!pin || !participantToken) return;

            try {
                const quizApiLocal = createQuizApiLocal(participantToken);

                const res = await quizApiLocal.get(`/quiz/${pin}?_t=${Date.now()}`);
                const data = res.data;

                const fetchedParticipants: LobbyParticipant[] = ((data.participants || []) as RawParticipantLike[])
                    .map((p) => {
                        const id = typeof p.id === 'number' ? p.id : typeof p.participantId === 'number' ? p.participantId : null;
                        if (id == null) return null;

                        return {
                            id,
                            nickname: typeof p.nickname === 'string' ? p.nickname : 'Anonym',
                            role: typeof p.role === 'string' ? p.role : 'USER',
                            isConnected: p.isConnected !== false
                        };
                    })
                    .filter((p): p is LobbyParticipant => p !== null);

                const fetchedDecks: LobbyDeck[] = ((data.decks || []) as RawDeckLike[])
                    .map((d) => {
                        const deckId = typeof d.deckId === 'number' ? d.deckId : typeof d.id === 'number' ? d.id : null;
                        if (deckId == null) return null;

                        return {
                            deckId,
                            name: typeof d.title === 'string' ? d.title : `Sada k procvičení #${deckId}`
                        };
                    })
                    .filter((d): d is LobbyDeck => d !== null);

                setLobbyData({
                    pin: data.pin,
                    currentState: data.currentState,
                    participants: fetchedParticipants,
                    decks: fetchedDecks
                });

            } catch (err) {
                console.error("Chyba při načítání snapshotu lobby", err);
                alert("Lobby neexistuje nebo bylo ukončeno.");
                navigate('/quiz');
            }
        };

        fetchLobby();
    }, [pin, participantToken, navigate]);


    //###################################
    // CHANGE LISTENING
    //###################################
    useEffect(() => {
        if (!events.presenceEvent) return;
        const ev = events.presenceEvent as { participantId?: number; id?: number; nickname?: string; status?: string; role?: string };

        const rawPId = ev.participantId ?? ev.id;
        const pId = rawPId != null ? Number(rawPId) : null;
        const nickname = typeof ev.nickname === 'string' ? ev.nickname : 'Anonym';
        const status = typeof ev.status === 'string' ? ev.status : '';
        const role = typeof ev.role === 'string' ? ev.role : 'USER';

        if (pId == null || isNaN(pId)) return;

        const timer = window.setTimeout(() => {
            setLobbyData(prev => {
                if (!prev) return prev;
                let updatedParticipants = [...(prev.participants || [])];
                const existingIndex = updatedParticipants.findIndex(p => p.id === pId);

                if (status === 'JOINED' || status === 'CONNECTED') {
                    if (existingIndex !== -1) {
                        updatedParticipants[existingIndex].isConnected = true;
                    } else {
                        updatedParticipants.push({ id: pId, nickname, role: role || 'USER', isConnected: true });
                    }
                } else if (status === 'DISCONNECTED') {
                    if (existingIndex !== -1) {
                        updatedParticipants[existingIndex].isConnected = false;
                    }
                } else if (status === 'LEFT' || status === 'KICKED') {
                    updatedParticipants = updatedParticipants.filter(p => p.id !== pId);
                }

                return { ...prev, participants: updatedParticipants };
            });
        }, 0);

        return () => window.clearTimeout(timer);
    }, [events.presenceEvent]);


    //##################################################################
    // DECK CHANGE LISTENER
    //##################################################################
    useEffect(() => {
        if (!events.deckEvent || !pin) return;
        const quizApiLocal = createQuizApiLocal(participantToken);
        quizApiLocal.get(`/quiz/${pin}?_t=${Date.now()}`)
            .then(res => {
                const fetchedDecks: LobbyDeck[] = ((res.data.decks || []) as RawDeckLike[])
                    .map((d) => {
                        const deckId = typeof d.deckId === 'number' ? d.deckId : typeof d.id === 'number' ? d.id : null;
                        if (deckId == null) return null;

                        return {
                            deckId,
                            name: typeof d.title === 'string' ? d.title : `Sada k procvičení #${deckId}`
                        };
                    })
                    .filter((d): d is LobbyDeck => d !== null);
                setLobbyData(prev => prev ? { ...prev, decks: fetchedDecks } : null);
            })
            .catch(err => console.error("Chyba při aktualizaci sad přes WS", err));
    }, [events.deckEvent, pin, participantToken]);


    //###################################################################
    // REDIRECT
    //###################################################################
    useEffect(() => {
        if (
            events.gameStarted ||
            events.currentQuestion ||
            lobbyData?.currentState === 'QUESTION_ACTIVE' ||
            lobbyData?.currentState === 'QUIZ_STARTING'
        ) {
            navigate(`/quiz/${pin}/play`, {
                state: { isHost, participantToken, participantId }
            });
        }
    }, [events.gameStarted, events.currentQuestion, lobbyData?.currentState, navigate, pin, isHost, participantToken, participantId]);


    useEffect(() => {
        if (!isAddModalOpen) return;

        const fetchSearchDecks = async () => {
            setIsSearching(true);
            try {
                let endpoint = '/decks';
                const params: Record<string, string | number> = { page: 0, size: 20 };

                if (searchTab === 'all' && searchKeyword.trim()) {
                    endpoint = "/decks/search";
                    params.keyword = searchKeyword.trim();
                } else if (searchTab === 'my') {
                    endpoint = '/decks/my';
                } else if (searchTab === 'favorites') {
                    endpoint = '/decks/favorites';
                }

                const response = await api.get(endpoint, { params });
                setSearchResults(response.data.content || []);
            } catch (err) {
                console.error("Chyba při vyhledávání sad:", err);
            } finally {
                setIsSearching(false);
            }
        };

        const timer = setTimeout(fetchSearchDecks, 300);
        return () => clearTimeout(timer);
    }, [searchTab, searchKeyword, isAddModalOpen]);

    //###################################################################
    // HANDLERS
    //###################################################################

    const handleStartQuiz = async () => {
        try {
            const quizApiLocal = createQuizApiLocal(participantToken);
            await quizApiLocal.post(`/quiz/${pin}/start`);
        } catch (err) {
            console.error('Chyba při startu', err);
        }
    };

    const handleKickPlayer = async (targetId: number) => {
        try {
            const quizApiLocal = createQuizApiLocal(participantToken);
            await quizApiLocal.delete(`/quiz/${pin}/participants/${targetId}`);
        } catch (err) {
            console.error('Chyba při vyhazování hráče', err);
        }
    };

    const handleAddDeck = async (deckId: number) => {
        try {
            const quizApiLocal = createQuizApiLocal(participantToken);
            const deckToAdd = searchResults.find(d => d.id === deckId);
            if (!deckToAdd) return;

            await quizApiLocal.post(`/quiz/${pin}/decks/${deckId}`);

            setLobbyData(prev => {
                if (!prev) return prev;
                if (prev.decks?.some(d => d.deckId === deckId)) return prev;
                return {
                    ...prev,
                    decks: [...(prev.decks || []), { deckId, name: deckToAdd.title }]
                };
            });
        } catch (err) {
            console.error('Chyba při přidání sady', err);
            alert('Sadu se nepodařilo přidat! Zkontroluj logy backendu.');
        }
    };

    const handleRemoveDeck = async (deckId: number) => {
        try {
            const quizApiLocal = createQuizApiLocal(participantToken);
            await quizApiLocal.delete(`/quiz/${pin}/decks/${deckId}`);

            setLobbyData(prev => {
                if (!prev) return prev;
                return {
                    ...prev,
                    decks: (prev.decks || []).filter(d => d.deckId !== deckId)
                };
            });
        } catch (err) {
            console.error('Chyba při odebírání sady', err);
        }
    };

    const handleLeave = async () => {
        if (window.confirm("Opravdu chcete opustit místnost?")) {
            try {
                const quizApiLocal = createQuizApiLocal(participantToken);
                await quizApiLocal.delete(`/quiz/${pin}/leave`);
            } catch (err) {
                console.error('Chyba při opouštění místnosti', err);
            } finally {
                navigate('/quiz');
            }
        }
    };

    if (!lobbyData) return <div className="p-10 text-center text-[var(--text)]">Načítání lobby...</div>;

    return (
        <div className="max-w-6xl mx-auto p-4 mt-2 sm:mt-6 flex flex-col md:flex-row gap-6 items-start relative">

            {/* Mobile SIDEBAR */}
            <div className="w-full md:w-72 border border-[var(--border)] bg-[var(--bg)] rounded-2xl flex flex-col shadow-sm md:sticky md:top-6">

                <button
                    onClick={() => setIsMobileSidebarOpen(!isMobileSidebarOpen)}
                    className="md:hidden flex items-center justify-between p-4 font-bold text-[var(--text-h)] outline-none"
                >
                    <div className="flex items-center gap-3">
                        <span className="text-lg font-black tracking-widest bg-[var(--accent-bg)] px-3 py-1 rounded-xl border border-[var(--border)] shadow-inner">
                            PIN: {pin}
                        </span>
                        <span className="text-sm font-bold text-[var(--text)]">
                            Hráči ({lobbyData.participants?.length || 0})
                        </span>
                    </div>
                    <span className={`text-xl transition-transform duration-300 ${isMobileSidebarOpen ? 'rotate-180' : ''}`}>
                        ▼
                    </span>
                </button>

                {/* Sidebar */}
                <div className={`p-4 sm:p-5 flex-col gap-6 md:flex ${isMobileSidebarOpen ? 'flex border-t border-[var(--border)]' : 'hidden'}`}>

                    <div className="text-center hidden md:block">
                        <h3 className="text-xs font-bold mb-2 uppercase tracking-widest text-[var(--text)] opacity-70">Kód místnosti</h3>
                        <div className="text-4xl sm:text-5xl font-black py-4 border border-[var(--border)] bg-[var(--accent-bg)] text-[var(--text-h)] rounded-2xl shadow-inner tracking-widest">
                            {pin}
                        </div>
                        {isHost && (
                            <div className="mt-3 inline-block px-3 py-1 rounded-full text-xs font-black uppercase bg-[var(--accent)] text-[var(--bg)] shadow-sm">
                                Jste hostitel
                            </div>
                        )}
                    </div>

                    {/* Host pin */}
                    {isHost && (
                        <div className="md:hidden text-center -mt-2 mb-2">
                            <div className="inline-block px-3 py-1 rounded-full text-xs font-black uppercase bg-[var(--accent)] text-[var(--bg)] shadow-sm">
                                Jste hostitel
                            </div>
                        </div>
                    )}

                    {/* Participant list */}
                    <div>
                        <h3 className="text-sm font-bold mb-3 uppercase flex justify-between border-b border-[var(--border)] pb-2 text-[var(--text)] hidden md:flex">
                            Hráči
                            <span className="font-black text-[var(--text-h)]">({lobbyData.participants?.length || 0})</span>
                        </h3>
                        <div className="flex flex-col gap-2 max-h-[40vh] md:max-h-none overflow-y-auto pr-1 custom-scrollbar">
                            {lobbyData.participants?.length === 0 ? (
                                <div className="text-center text-sm italic text-[var(--text)]">Nikdo není připojen</div>
                            ) : (
                                lobbyData.participants?.map(p => {
                                    const isMe = String(p.id) === String(participantId);
                                    const isPlayerConnected = isMe ? isConnected : p.isConnected;

                                    return (
                                        <div key={p.id} className={`flex justify-between items-center p-3 rounded-xl border border-[var(--border)] bg-[var(--bg)] text-sm transition-all ${isPlayerConnected ? 'opacity-100' : 'opacity-40'}`}>
                                            <div className="flex items-center gap-3 truncate">
                                                {p.role === 'HOST' || p.role === 'ROLE_HOST' ? (
                                                    <span className="text-xl leading-none">👑</span>
                                                ) : (
                                                    <div className={`w-3 h-3 rounded-full ${isPlayerConnected ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.4)]' : 'bg-zinc-400'}`}></div>
                                                )}
                                                <span className={`truncate text-[var(--text-h)] ${isMe ? 'font-black' : 'font-bold'}`}>
                                                    {p.nickname} {isMe && <span className="ml-1 opacity-50 text-xs">(Vy)</span>}
                                                </span>
                                            </div>
                                            {isHost && !isMe && (
                                                <button onClick={() => handleKickPlayer(p.id)} className="text-red-500 hover:bg-red-500/10 w-8 h-8 flex items-center justify-center rounded-lg transition-colors text-xl">&times;</button>
                                            )}
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Main body */}
            <div className="flex-1 w-full flex flex-col gap-6">

                {/* Top bar */}
                <div className="flex flex-col-reverse sm:flex-row justify-between items-stretch sm:items-center pb-4 border-b border-[var(--border)] gap-4">
                    <button onClick={handleLeave} className="px-6 py-3 border border-[var(--border)] text-[var(--text)] rounded-xl font-bold transition-colors hover:bg-[var(--accent-bg)] w-full sm:w-auto">
                        Odejít
                    </button>

                    {isHost ? (
                        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                            <label className="flex justify-center items-center gap-2 cursor-pointer font-bold select-none px-4 py-3 border border-[var(--border)] bg-[var(--bg)] text-[var(--text-h)] rounded-xl transition-colors hover:bg-[var(--accent-bg)]">
                                <input
                                    type="checkbox"
                                    checked={isHostPlaying}
                                    onChange={(e) => setIsHostPlaying(e.target.checked)}
                                    className="w-5 h-5 cursor-pointer accent-[var(--accent)]"
                                />
                                Hraji také
                            </label>

                            <button
                                onClick={handleStartQuiz}
                                disabled={!lobbyData.participants?.length || !lobbyData.decks?.length}
                                className="px-8 py-3 rounded-xl font-bold text-lg transition-transform active:scale-95 disabled:opacity-50 shadow-md bg-[var(--accent)] text-[var(--bg)]"
                            >
                                Zahájit kvíz
                            </button>
                        </div>
                    ) : (
                        <div className="px-6 py-3 font-bold rounded-xl text-center bg-emerald-500/10 text-emerald-600 border border-emerald-500/20">
                            <span className="animate-pulse">Čeká se na zahájení...</span>
                        </div>
                    )}
                </div>

                {/* Added decks */}
                <div>
                    <h2 className="text-2xl font-black mb-4 text-[var(--text-h)]">Vybrané sady</h2>

                    <div className="space-y-3">
                        {lobbyData.decks?.length === 0 ? (
                            <div className="p-8 text-center border-2 border-dashed rounded-xl border-[var(--border)] text-[var(--text)]">
                                Zatím nebyly přidány žádné sady k procvičení.
                            </div>
                        ) : (
                            lobbyData.decks?.map(deck => (
                                <div key={deck.deckId} className="flex flex-col sm:flex-row justify-between items-start sm:items-center p-4 sm:p-5 border border-[var(--border)] bg-[var(--bg)] rounded-xl shadow-sm gap-3 transition-all">
                                    <span className="font-bold text-lg text-[var(--text-h)] line-clamp-2">
                                        {deck.name}
                                    </span>
                                    {isHost && (
                                        <button
                                            onClick={() => handleRemoveDeck(deck.deckId)}
                                            className="w-full sm:w-auto px-4 py-2 rounded-lg text-sm font-bold transition-colors bg-red-500/10 text-red-500 hover:bg-red-500 hover:text-white border border-red-500/20"
                                        >
                                            Odebrat
                                        </button>
                                    )}
                                </div>
                            ))
                        )}

                        {isHost && (
                            <button
                                onClick={() => setIsAddModalOpen(true)}
                                className="w-full py-4 mt-2 text-center border-2 border-dashed rounded-xl font-bold transition-all hover:opacity-70 bg-[var(--accent-bg)] border-[var(--accent-border)] text-[var(--text-h)]"
                            >
                                + Přidat sadu z knihovny
                            </button>
                        )}
                    </div>
                </div>
            </div>

            {/* Add deck modal window */}
            {isAddModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
                    <div className="w-full max-w-3xl flex flex-col shadow-2xl overflow-hidden rounded-2xl bg-[var(--bg)] border border-[var(--border)] max-h-[90vh] sm:max-h-[85vh]">

                        <div className="p-4 sm:p-5 border-b border-[var(--border)] flex justify-between items-center bg-[var(--accent-bg)]">
                            <h2 className="text-xl font-black text-[var(--text-h)]">Přidat sadu</h2>
                            <button onClick={() => setIsAddModalOpen(false)} className="text-3xl leading-none hover:opacity-70 transition-opacity text-[var(--text)]">&times;</button>
                        </div>

                        <div className="p-4 sm:p-5 flex flex-col flex-1 overflow-hidden gap-4">
                            {/* Tabs */}
                            <div className="flex space-x-4 sm:space-x-6 border-b border-[var(--border)] overflow-x-auto custom-scrollbar">
                                {[
                                    { id: 'all' as const, label: 'Všechny sady' },
                                    { id: 'my' as const, label: 'Moje sady' },
                                    { id: 'favorites' as const, label: 'Oblíbené' }
                                ].map(tab => (
                                    <button
                                        key={tab.id}
                                        className={`font-bold pb-3 transition-colors whitespace-nowrap border-b-2 ${searchTab === tab.id ? 'text-[var(--text-h)] border-[var(--text-h)]' : 'text-[var(--text)] border-transparent hover:text-[var(--text-h)]'}`}
                                        onClick={() => { setSearchTab(tab.id); setSearchKeyword(''); }}
                                    >
                                        {tab.label}
                                    </button>
                                ))}
                            </div>

                            {/* Search input */}
                            {searchTab === 'all' && (
                                <input
                                    type="text"
                                    placeholder="Hledat podle názvu sady..."
                                    className="w-full p-3 sm:p-4 rounded-xl outline-none border-2 transition-colors bg-[var(--bg)] border-[var(--border)] text-[var(--text-h)] focus:border-[var(--accent)]"
                                    value={searchKeyword}
                                    onChange={(e) => setSearchKeyword(e.target.value)}
                                />
                            )}

                            {/* Search results */}
                            <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                                {isSearching ? (
                                    <div className="text-center py-10 opacity-70 text-[var(--text)] font-medium">Načítám...</div>
                                ) : searchResults.length === 0 ? (
                                    <div className="text-center py-10 opacity-70 text-[var(--text)] font-medium">Žádné sady nenalezeny.</div>
                                ) : (
                                    searchResults.map(deck => {
                                        const isAdded = lobbyData.decks?.some(d => d.deckId === deck.id) || false;

                                        return (
                                            <div key={deck.id} className="p-4 border rounded-xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 transition-all bg-[var(--bg)] border-[var(--border)]">
                                                <div>
                                                    <h3 className="font-bold text-lg text-[var(--text-h)] line-clamp-1">{deck.title}</h3>
                                                    <p className="text-sm mt-1 text-[var(--text)]">
                                                        Autor: <span className="font-bold text-[var(--text-h)]">{deck.authorName}</span> • {deck.numberOfQuestions} otázek
                                                    </p>
                                                </div>
                                                <button
                                                    onClick={() => {
                                                        if (isAdded) handleRemoveDeck(deck.id);
                                                        else handleAddDeck(deck.id);
                                                    }}
                                                    className={`w-full sm:w-auto px-6 py-2 font-bold text-sm rounded-lg transition-colors border ${isAdded ? 'opacity-50 cursor-not-allowed bg-[var(--accent-bg)] text-[var(--text)] border-transparent' : 'bg-[var(--accent)] text-[var(--bg)] border-[var(--accent)] hover:opacity-90 active:scale-95'}`}
                                                >
                                                    {isAdded ? 'Přidáno' : 'Přidat'}
                                                </button>
                                            </div>
                                        );
                                    })
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}