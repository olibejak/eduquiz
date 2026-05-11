import { useState, useEffect } from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import { useQuizContext } from '../../hooks/useQuizContext';

export default function QuizPlay() {
    const navigate = useNavigate();
    const location = useLocation();

    const { ws, isHost: contextIsHost, participantId: contextId, isHostPlaying } = useQuizContext();
    const { events, actions } = ws;

    const isHost = contextIsHost ?? location.state?.isHost;
    const participantId = contextId ?? location.state?.participantId;

    const phase: 'WAITING' | 'QUESTION' | 'RESULTS' | 'FINISHED' = events.quizFinished
        ? 'FINISHED'
        : events.questionResults
            ? 'RESULTS'
            : events.currentQuestion
                ? 'QUESTION'
                : 'WAITING';

    const [answeredQuestionId, setAnsweredQuestionId] = useState<number | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(30);

    const [textAnswer, setTextAnswer] = useState<string>('');
    const [matchingSelection, setMatchingSelection] = useState<number | null>(null);
    const [matchingPairs, setMatchingPairs] = useState<Record<number, number>>({});
    const [shuffledAnswers, setShuffledAnswers] = useState<any[]>([]);
    const [prevQuestionId, setPrevQuestionId] = useState<number | null>(null);

    const questionEvent = events.currentQuestion as any;
    const resultsEvent = events.questionResults as any;

    const question = questionEvent?.question;
    const results = resultsEvent?.results;

    const hasAnswered = !!question && answeredQuestionId === question.id;

    const isModeratorOnly = isHost && !isHostPlaying;

    const isLocked = hasAnswered || timeLeft === 0 || isModeratorOnly;

    const myResult = results?.participantResults?.find((r: any) => r.participantId === participantId);
    const fullAnswers = results?.question?.answers || results?.answers || [];
    const correctAnswerIds = fullAnswers
        .filter((ans: any) => ans.payload?.isCorrect === true)
        .map((ans: any) => ans.id);

    const qType = question?.questionType || '';
    const isChoice = qType === '' || qType.includes('CHOICE');
    const isStandard = qType.includes('WRITE') || qType.includes('NUMERIC') || qType.includes('STANDARD');
    const isMatching = qType.includes('MATCHING');

    if (question && question.id !== prevQuestionId) {
        setPrevQuestionId(question.id);
        setTimeLeft(question.timeLimit || question.duration || 30);
        setTextAnswer('');
        setMatchingSelection(null);
        setMatchingPairs({});

        if (question.answers) {
            setShuffledAnswers([...question.answers].sort(() => Math.random() - 0.5));
        } else {
            setShuffledAnswers([]);
        }
    }

    useEffect(() => {
        if (phase === 'QUESTION' && question?.id) {
            const timer = setInterval(() => {
                setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
            }, 1000);
            return () => clearInterval(timer);
        }
    }, [phase, question?.id]);

    const handleNextQuestion = () => actions.sendNextQuestion();

    const handleChoiceAnswer = (answerId: number) => {
        if (isLocked || !question?.id) return;
        actions.sendAnswer(participantId, question.id, 'CHOICE', {
            answerType: 'CHOICE',
            answerId: answerId
        });
        setAnsweredQuestionId(question.id);
    };

    const handleStandardAnswer = () => {
        if (isLocked || !question?.id || !textAnswer.trim()) return;
        actions.sendAnswer(participantId, question.id, 'STANDARD', {
            answerType: 'STANDARD',
            text: textAnswer.trim()
        });
        setAnsweredQuestionId(question.id);
    };

    const handleMatchingClick = (answerId: number) => {
        if (isLocked) return;
        if (matchingPairs[answerId] !== undefined) {
            const partnerId = matchingPairs[answerId];
            const newPairs = { ...matchingPairs };
            delete newPairs[answerId];
            delete newPairs[partnerId];
            setMatchingPairs(newPairs);
            return;
        }
        if (matchingSelection === null) {
            setMatchingSelection(answerId);
        } else {
            if (matchingSelection === answerId) {
                setMatchingSelection(null);
            } else {
                setMatchingPairs(prev => ({ ...prev, [matchingSelection]: answerId, [answerId]: matchingSelection }));
                setMatchingSelection(null);
            }
        }
    };

    const handleMatchingSubmit = () => {
        if (isLocked || !question?.id) return;
        const matchesToSend: Record<number, number> = {};
        const seen = new Set<number>();
        Object.entries(matchingPairs).forEach(([k, v]) => {
            const key = Number(k);
            if (!seen.has(key) && !seen.has(v)) {
                matchesToSend[key] = v;
                seen.add(key);
                seen.add(v);
            }
        });
        actions.sendAnswer(participantId, question.id, 'MATCHING', {
            answerType: 'MATCHING',
            matches: matchesToSend
        });
        setAnsweredQuestionId(question.id);
    };

    const pairSymbols = ['🔹', '🔸', '🟢', '🟣', '⭐', '🔺', '♥️', '♣️'];
    const getPairIndex = (id: number) => {
        if (matchingPairs[id] === undefined) return -1;
        const minId = Math.min(id, matchingPairs[id]);
        const allMinIds = Array.from(new Set(Object.entries(matchingPairs).map(([k, v]) => Math.min(Number(k), v)))).sort((a, b) => a - b);
        return allMinIds.indexOf(minId);
    };

    if (phase === 'WAITING') return <div className="flex flex-col items-center justify-center min-h-[80vh] text-center px-4"><h1 className="text-2xl md:text-4xl font-bold mb-6 animate-pulse text-[var(--text-h)]">Načítání otázky...</h1></div>;

    if (phase === 'FINISHED') {
        const leaderboard = [...(results?.participantResults || [])].sort((a, b) => b.currentScore - a.currentScore);

        return (
            <div className="flex flex-col items-center justify-center min-h-[80vh] w-full max-w-2xl mx-auto px-4 py-8">
                <h1 className="text-4xl md:text-5xl font-black mb-2 text-center text-[var(--text-h)]">Kvíz skončil!</h1>
                <p className="text-lg md:text-xl mb-8 text-[var(--text)] font-medium">Tady je konečné pořadí</p>

                {/* Results Table */}
                <div className="w-full border border-[var(--border)] bg-[var(--bg)] rounded-2xl overflow-x-auto shadow-sm mb-10">
                    <table className="w-full text-left border-collapse min-w-[300px]">
                        <thead>
                        <tr className="bg-[var(--accent-bg)] text-[var(--text-h)] border-b border-[var(--border)]">
                            <th className="p-3 md:p-4 font-black">#</th>
                            <th className="p-3 md:p-4 font-black">Přezdívka</th>
                            <th className="p-3 md:p-4 font-black text-right">Body</th>
                        </tr>
                        </thead>
                        <tbody>
                        {leaderboard.map((res, index) => {
                            const position = index + 1;
                            return (
                                <tr key={res.participantId} className="border-b border-[var(--border)] last:border-0 hover:bg-[var(--accent-bg)] transition-colors">
                                    <td className={`p-3 md:p-4 font-black ${index === 0 ? 'text-yellow-500' : 'text-[var(--text)]'}`}>
                                        {position === 1 ? '🥇' : position === 2 ? '🥈' : position === 3 ? '🥉' : `${position}.`}
                                    </td>
                                    <td className="p-3 md:p-4 font-bold text-[var(--text-h)]">
                                        {res.nickname}
                                        {res.participantId === participantId && <span className="ml-2 text-xs md:text-sm text-[var(--text)] bg-[var(--code-bg)] px-2 py-1 rounded-md">Vy</span>}
                                    </td>
                                    <td className="p-3 md:p-4 font-black text-right text-[var(--text-h)]">
                                        {res.currentScore.toLocaleString()}
                                    </td>
                                </tr>
                            )})}
                        </tbody>
                    </table>
                </div>

                <button
                    onClick={() => navigate('/quiz')}
                    className="w-full md:w-auto px-8 py-4 rounded-xl font-bold border-2 transition-all hover:bg-[var(--accent-bg)] active:scale-95 bg-[var(--bg)] text-[var(--text-h)] border-[var(--border)]"
                >
                    Zpět do menu
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-4 md:p-6 flex flex-col min-h-[85vh]">
            <div className="text-center mb-6 pt-4 md:pt-8 flex flex-col items-center">
                {phase === 'QUESTION' && (
                    <div className={`inline-flex items-center justify-center w-16 h-16 md:w-20 md:h-20 rounded-full border-4 font-black text-2xl md:text-3xl mb-4 transition-colors duration-300 bg-[var(--bg)] ${timeLeft <= 5 ? 'border-red-500 text-red-500 shadow-[0_0_15px_rgba(239,68,68,0.3)]' : 'border-[var(--accent)] text-[var(--text-h)]'}`}>
                        {timeLeft}
                    </div>
                )}
                <h2 className="text-2xl md:text-4xl font-bold leading-snug text-[var(--text-h)]">{question?.text}</h2>
            </div>

            {phase === 'QUESTION' && (
                <div className="flex-1 flex flex-col justify-center w-full">
                    {!isModeratorOnly && isLocked ? (
                        <div className="text-center p-8 md:p-12 text-xl md:text-2xl font-bold animate-pulse text-[var(--text-h)] bg-[var(--accent-bg)] rounded-3xl border border-[var(--border)]">
                            {hasAnswered ? 'Odpověď odeslána. Čekáme na ostatní!' : 'Čas vypršel! Zpracovávám výsledky...'}
                        </div>
                    ) : (
                        <div className="w-full">
                            {isChoice && (
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4">
                                    {question?.answers?.map((ans: any) => (
                                        <button
                                            key={ans.id}
                                            onClick={() => handleChoiceAnswer(ans.id)}
                                            disabled={isLocked}
                                            className={`p-6 md:p-8 rounded-2xl font-bold text-lg md:text-2xl shadow-sm flex items-center justify-center min-h-[100px] md:min-h-[120px] transition-all border-2 bg-[var(--bg)] text-[var(--text-h)] border-[var(--border)] ${!isLocked ? 'active:scale-95 hover:border-[var(--accent)] cursor-pointer' : 'opacity-70 cursor-not-allowed'}`}
                                        >
                                            {ans.text}
                                        </button>
                                    ))}
                                </div>
                            )}

                            {isStandard && (
                                <div className="flex flex-col items-center gap-4 md:gap-6 w-full max-w-md mx-auto">
                                    <input
                                        type={question.questionType?.includes('NUMERIC') ? 'number' : 'text'}
                                        value={textAnswer}
                                        onChange={(e) => setTextAnswer(e.target.value)}
                                        placeholder="Zadejte vaši odpověď..."
                                        disabled={isLocked}
                                        className="w-full p-4 md:p-6 text-xl md:text-2xl font-bold text-center rounded-2xl border-4 outline-none transition-colors bg-[var(--bg)] text-[var(--text-h)] border-[var(--border)] focus:border-[var(--accent)]"
                                    />
                                    <button
                                        onClick={handleStandardAnswer}
                                        disabled={isLocked || !textAnswer.trim()}
                                        className={`w-full py-4 rounded-xl font-black text-lg md:text-xl transition-transform bg-[var(--accent)] text-[var(--bg)] ${!isLocked && textAnswer.trim() ? 'active:scale-95 cursor-pointer hover:opacity-90' : 'opacity-50 cursor-not-allowed'} mt-2`}
                                    >
                                        Odeslat odpověď
                                    </button>
                                </div>
                            )}

                            {isMatching && (
                                <div className="flex flex-col gap-4 md:gap-6">
                                    <div className="text-center font-bold mb-2 text-[var(--text)] bg-[var(--accent-bg)] py-2 rounded-lg text-sm md:text-base">
                                        Spojte správné pojmy k sobě (klikni na první, pak na druhý).
                                    </div>
                                    <div className="grid grid-cols-2 gap-3 md:gap-4">
                                        {shuffledAnswers.map((ans) => {
                                            const isSelected = matchingSelection === ans.id;
                                            const pairIdx = getPairIndex(ans.id);
                                            const isPaired = pairIdx !== -1;
                                            return (
                                                <button
                                                    key={ans.id}
                                                    onClick={() => handleMatchingClick(ans.id)}
                                                    disabled={isLocked}
                                                    className={`p-3 md:p-4 rounded-xl font-bold text-sm md:text-lg transition-all border-2 md:border-4 flex items-center justify-center min-h-[80px] md:min-h-[100px] ${isSelected ? 'scale-[1.02] shadow-md border-[var(--accent)] bg-[var(--accent-bg)]' : isPaired ? 'border-[var(--accent)] bg-[var(--accent-bg)] opacity-70' : 'border-[var(--border)] bg-[var(--bg)]'} text-[var(--text-h)] ${!isLocked ? 'cursor-pointer hover:bg-[var(--accent-bg)] active:scale-95' : 'cursor-not-allowed'}`}
                                                >
                                                    {isPaired && <span className="mr-2 text-lg md:text-xl">{pairSymbols[pairIdx % pairSymbols.length]}</span>}
                                                    {ans.text}
                                                </button>
                                            );
                                        })}
                                    </div>
                                    <button
                                        onClick={handleMatchingSubmit}
                                        disabled={isLocked || Object.keys(matchingPairs).length === 0}
                                        className={`w-full max-w-md mx-auto py-4 rounded-xl font-black text-lg md:text-xl transition-transform mt-4 bg-[var(--accent)] text-[var(--bg)] ${!isLocked && Object.keys(matchingPairs).length > 0 ? 'active:scale-95 cursor-pointer hover:opacity-90' : 'opacity-50 cursor-not-allowed'}`}
                                    >
                                        Odeslat spojení
                                    </button>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {phase === 'RESULTS' && (
                <div className="flex-1 flex flex-col w-full mt-2 md:mt-4">
                    {/* Zobrazení osobního výsledku pro běžného hráče */}
                    {!isModeratorOnly && myResult && (
                        <div className={`text-center p-6 md:p-8 rounded-3xl w-full mb-6 md:mb-8 border-2 ${myResult.isCorrect ? 'bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400' : 'bg-red-500/10 border-red-500 text-red-600 dark:text-red-400'}`}>
                            <h2 className="text-3xl md:text-4xl font-black mb-2">{myResult.isCorrect ? 'Správně! 🎉' : 'Špatně! 😢'}</h2>
                            <p className="text-lg md:text-xl font-bold opacity-90 text-[var(--text-h)]">Vaše aktuální skóre: {myResult.currentScore} bodů</p>
                        </div>
                    )}

                    {/* Zobrazení správných odpovědí */}

                    {/* MULTIPLE CHOICE */}
                    {isChoice && (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4 w-full">
                            {fullAnswers.map((ans: any) => {
                                const isCorrectAnswer = correctAnswerIds.includes(ans.id);
                                return (
                                    <div key={ans.id} className={`p-5 md:p-6 rounded-2xl font-bold text-lg md:text-xl flex items-center justify-center min-h-[90px] md:min-h-[100px] transition-all border-2 ${isCorrectAnswer ? 'bg-emerald-500/10 border-emerald-500 text-[var(--text-h)]' : 'bg-[var(--bg)] border-[var(--border)] text-[var(--text)] opacity-50'}`}>
                                        {ans.text} {isCorrectAnswer && <span className="ml-3 text-2xl md:text-3xl text-emerald-500">✓</span>}
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* STANDARD / WRITE */}
                    {isStandard && (
                        <div className="flex flex-col items-center gap-3 md:gap-4 w-full mt-4">
                            <h3 className="text-lg md:text-xl font-bold text-[var(--text)]">Správná odpověď:</h3>
                            <div className="p-5 md:p-6 rounded-2xl font-black text-2xl md:text-3xl border-2 w-full max-w-md text-center shadow-sm bg-emerald-500/10 border-emerald-500 text-emerald-600 dark:text-emerald-400">
                                {fullAnswers.map((ans: any) => ans.text).join(' nebo ')}
                            </div>
                        </div>
                    )}

                    {/* MATCHING */}
                    {isMatching && (
                        <div className="flex flex-col items-center gap-3 md:gap-4 w-full mt-4">
                            <h3 className="text-lg md:text-xl font-bold text-[var(--text)]">Správné dvojice:</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4 w-full max-w-4xl">
                                {Object.values(
                                    fullAnswers.reduce((acc: any, ans: any) => {
                                        const mId = ans.payload?.matchId;
                                        if (mId != null) {
                                            if (!acc[mId]) acc[mId] = [];
                                            acc[mId].push(ans.text);
                                        }
                                        return acc;
                                    }, {})
                                ).map((pair: any, idx) => (
                                    <div key={idx} className="p-3 md:p-4 rounded-xl font-bold text-base md:text-lg flex items-center justify-between border-2 shadow-sm bg-emerald-500/10 border-emerald-500 text-[var(--text-h)]">
                                        <span className="flex-1 text-right break-words">{pair[0]}</span>
                                        <span className="mx-3 md:mx-4 text-xl md:text-2xl text-emerald-500">↔</span>
                                        <span className="flex-1 text-left break-words">{pair[1]}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Next question button */}
                    {isHost && (
                        <div className="mt-8 md:mt-12 text-center w-full">
                            <button onClick={handleNextQuestion} className="w-full md:w-auto px-10 py-4 md:py-5 rounded-2xl font-black text-xl md:text-2xl active:scale-95 shadow-xl bg-[var(--accent)] text-[var(--bg)] hover:opacity-90">
                                Další otázka ➔
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}