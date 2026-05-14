import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/axios';

interface UserHistoryItemDTO {
    sessionId: string;
    deckTitles: string[];
    playedAt: string;
    myScore: number;
    myPosition: number;
}

interface LeaderboardRowDTO {
    nickname: string;
    score: number;
    position: number;
}

interface QuizLeaderboardDTO {
    sessionId: string;
    deckTitles: string[];
    playedAt: string;
    players: LeaderboardRowDTO[];
}

export default function QuizHistoryWidget() {
    const { user } = useAuth();
    const [quizHistory, setQuizHistory] = useState<UserHistoryItemDTO[]>([]);
    const [isLoadingHistory, setIsLoadingHistory] = useState(true);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [leaderboard, setLeaderboard] = useState<QuizLeaderboardDTO | null>(null);
    const [isLoadingLeaderboard, setIsLoadingLeaderboard] = useState(true);

    useEffect(() => {
        if (!user) return;
        api.get('/users/history/me')
            .then((response) => {
                setQuizHistory(response.data || []);
            })
            .catch(err => console.log("Chyba při načítání historie kvízů", err))
            .finally(() => setIsLoadingHistory(false));
    }, [user]);

    const handleRowClick = async (sessionId: string) => {
        setIsModalOpen(true);
        setIsLoadingLeaderboard(true);
        setLeaderboard(null);

        api.get(`/users/history/sessions/${sessionId}`)
            .then((response) => {
                setLeaderboard(response.data || []);
            })
            .catch(err => console.log("Chyba při načítání výsledků kvízu", err))
            .finally(() => setIsLoadingLeaderboard(false));
    };

    if (!user) return null;

    return (
        <div className="space-y-4">
            <h2 className="text-xl font-bold text-(--text-h)">Historie kvízů</h2>

            {isLoadingHistory ? (
                <div className="p-6 text-center animate-pulse border rounded-xl bg-(--bg) border-(--border) text-(--text)">
                    Načítám historii...
                </div>
            ) : quizHistory.length === 0 ? (
                <div className="p-8 border-2 border-dashed rounded-xl text-center bg-(--bg) border-(--border) text-(--text)">
                    Zatím nemáš odehrané žádné kvízy.
                </div>
            ) : (
                <div className="grid gap-3">
                    {quizHistory.map((item) => (
                        <button
                            key={item.sessionId}
                            onClick={() => handleRowClick(item.sessionId)}
                            className="group p-4 border rounded-xl text-left transition-all hover:border-blue-400 hover:-translate-y-0.5 flex justify-between items-center shadow-sm bg-(--bg) border-(--border)"
                        >
                            <div>
                                <div className="font-semibold text-lg text-(--text-h)">
                                    {item.deckTitles.join(', ')}
                                </div>
                                <div className="text-sm opacity-75 mt-1 text-(--text)">
                                    {new Date(item.playedAt).toLocaleDateString('cs-CZ')} v {new Date(item.playedAt).toLocaleTimeString('cs-CZ', { hour: '2-digit', minute:'2-digit' })}
                                </div>
                            </div>

                            <div className="flex items-center gap-3 sm:gap-4">
                                <div className="text-right">
                                    <div className="font-bold text-lg text-(--accent)">
                                        {item.myPosition}. místo
                                    </div>
                                    <div className="text-sm opacity-75 text-(--text)">
                                        {item.myScore} b
                                    </div>
                                </div>
                                <div className="p-2 rounded-full opacity-0 group-hover:opacity-100 transition-all bg-blue-50 text-blue-600 hidden sm:block">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                                    </svg>
                                </div>
                            </div>
                        </button>
                    ))}
                </div>
            )}

            {/* MODAL LEADERBOARD */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
                    <div className="bg-(--bg) w-full max-w-lg rounded-xl border border-(--border) shadow-xl overflow-hidden flex flex-col max-h-[80vh]">
                        <div className="p-4 border-b border-(--border) flex justify-between items-center bg-(--accent-bg)/30">
                            <div>
                                <h3 className="text-lg font-bold text-(--text-h)">Výsledky</h3>
                                {leaderboard && (
                                    <p className="text-sm text-(--text)">
                                        {leaderboard.deckTitles.join(', ')} ({new Date(leaderboard.playedAt).toLocaleDateString('cs-CZ')})
                                    </p>
                                )}
                            </div>
                            <button onClick={() => setIsModalOpen(false)} className="text-(--text) hover:text-(--accent) transition-colors p-2 text-xl font-bold">
                                &times;
                            </button>
                        </div>
                        <div className="p-4 overflow-y-auto text-(--text)">
                            {isLoadingLeaderboard ? (
                                <div className="text-center py-8">Načítám výsledky...</div>
                            ) : leaderboard ? (
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                    <tr className="border-b-2 border-(--border)">
                                        <th className="py-2 px-2 text-center font-medium w-16">Pořadí</th>
                                        <th className="py-2 px-4 font-medium">Hráč</th>
                                        <th className="py-2 px-4 font-medium text-right">Skóre</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {leaderboard.players.map((player, idx) => (
                                        <tr key={idx} className={`border-b border-(--border) last:border-0 ${player.nickname === user?.username ? 'bg-(--accent-bg)/50 font-semibold' : ''}`}>
                                            <td className="py-3 px-2 text-center">
                                                {player.position === 1 ? '🥇' : player.position === 2 ? '🥈' : player.position === 3 ? '🥉' : `${player.position}.`}
                                            </td>
                                            <td className="py-3 px-4">{player.nickname}</td>
                                            <td className="py-3 px-4 text-right">{player.score}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            ) : (
                                <div className="text-center py-8 text-red-500">Výsledky se nepodařilo načíst.</div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}