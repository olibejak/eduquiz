import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../../api/axios';
import type { Question, FlashcardReview, FlashcardRating } from '../../types/flashcards';
import { useAuth } from "../../context/AuthContext.tsx";

const shuffleArray = <T,>(array: T[]): T[] => {
    const newArr = [...array];
    for (let i = newArr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [newArr[i], newArr[j]] = [newArr[j], newArr[i]];
    }
    return newArr;
};

export default function StudySession() {
    const { user } = useAuth();
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [questions, setQuestions] = useState<Question[]>([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [reviews, setReviews] = useState<FlashcardReview[]>([]);
    const [showAnswer, setShowAnswer] = useState(false);
    const [secondsElapsed, setSecondsElapsed] = useState(0);

    const [totalCardsDone, setTotalCardsDone] = useState(0);

    useEffect(() => {
        if (user) {
            api.get(`/flashcards/${id}/session`)
                .then(res => setQuestions(res.data))
                .catch(err => {
                    console.error("Nepodařilo se načíst session", err);
                    alert("Nepodařilo se načíst otázky.");
                    navigate(-1);
                });
        } else {
            api.get(`/decks/${id}`)
                .then(res => {
                    const deckQuestions = res.data.questions || [];
                    if (deckQuestions.length === 0) {
                        alert("Tato sada je zatím prázdná.");
                        navigate(-1);
                    }
                    setQuestions(shuffleArray(deckQuestions));
                })
                .catch(err => {
                    console.error("Nepodařilo se načíst sadu", err);
                    alert("Sadu se nepodařilo načíst.");
                    navigate(-1);
                });
        }

        const timer = setInterval(() => setSecondsElapsed(prev => prev + 1), 1000);
        return () => clearInterval(timer);
    }, [id, navigate, user]);

    const formatTime = (totalSeconds: number) => {
        const m = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
        const s = (totalSeconds % 60).toString().padStart(2, '0');
        return `${m}:${s}`;
    };

    const submitBatchAndExit = async () => {
        if (user && reviews.length > 0) {
            try {
                await api.post(`/flashcards/${id}`, { reviews });
            } catch (err) {
                console.error("Chyba při ukládání průběhu", err);
            }
        }
        navigate(user ? `/flashcards/${id}` : '/flashcards');
    };

    const handleRating = (rating: FlashcardRating) => {
        const currentQ = questions[currentIndex];
        let newReviews = reviews;

        if (user) {
            newReviews = [...reviews, { questionId: currentQ.id, rating }];
            setReviews(newReviews);
        }

        setTotalCardsDone(prev => prev + 1);

        if (currentIndex < questions.length - 1) {
            setCurrentIndex(prev => prev + 1);
            setShowAnswer(false);
        } else {
            if (user) {
                api.post(`/flashcards/${id}`, { reviews: newReviews })
                    .finally(() => navigate(`/flashcards/${id}`));
            } else {
                // NEPŘIHLÁŠENÝ: Znovu zamícháme balíček a jedeme nekonečnou smyčku!
                setQuestions(prev => shuffleArray([...prev]));
                setCurrentIndex(0);
                setShowAnswer(false);
            }
        }
    };

    const currentCard = questions[currentIndex];

    const { leftColumn, shuffledRightColumn } = useMemo(() => {
        if (!currentCard || currentCard.questionType !== 'MATCHING') {
            return { leftColumn: [], shuffledRightColumn: [] };
        }
        const left = currentCard.answers.filter(a => !a.payload?.associate);
        const right = currentCard.answers.filter(a => a.payload?.associate);
        return {
            leftColumn: left,
            shuffledRightColumn: shuffleArray(right)
        };
    }, [currentCard]);

    if (questions.length === 0) {
        return <div className="p-10 text-center flex items-center justify-center h-screen" style={{ color: 'var(--text)', backgroundColor: 'var(--bg)' }}>Načítám kartičky...</div>;
    }

    return (
        <div className="flex flex-col md:flex-row overflow-hidden" style={{ backgroundColor: 'var(--bg)' }}>

            {/* TOPBAR for mobile / SIDEBAR for desktop */}
            <div className="flex flex-row md:flex-col items-center md:items-stretch justify-between p-4 md:p-6 border-b md:border-b-0 md:border-r w-full md:w-64 flex-shrink-0" style={{ borderColor: 'var(--border)', backgroundColor: 'var(--bg)' }}>

                <div className="flex flex-row md:flex-col items-center md:items-start gap-4 md:gap-0 md:space-y-4">
                    <h2 className="hidden md:block text-xl font-bold" style={{ color: 'var(--text-h)' }}>Studium</h2>

                    <div className="flex flex-row md:flex-col items-center md:items-start gap-3 md:gap-0">
                        <div className="text-xl md:text-3xl font-mono font-bold md:mt-4" style={{ color: 'var(--text-h)' }}>
                            {formatTime(secondsElapsed)}
                        </div>
                        <div className="text-sm font-medium md:mt-2 px-2 py-1 md:px-0 md:py-0 rounded-md md:bg-transparent bg-opacity-20 var(--bg)" style={{ color: 'var(--text)' }}>
                            {user ? `${currentIndex + 1} / ${questions.length}` : `Projeto: ${totalCardsDone}`}
                        </div>
                    </div>
                </div>

                <button
                    onClick={submitBatchAndExit}
                    className="px-4 py-2 md:w-full border rounded font-semibold transition-colors hover:bg-red-500 hover:text-white text-sm md:text-base"
                    style={{ borderColor: 'var(--border)', color: 'var(--text)' }}
                >
                    {user ? <span>Uložit <span className="hidden md:inline">a odejít</span></span> : 'Ukončit studium'}
                </button>
            </div>

            {/* Flashcard */}
            <div className="flex-1 flex flex-col items-center justify-between md:justify-center p-4 md:p-10 overflow-y-auto">
                <div className="w-full max-w-3xl flex-1 md:flex-none md:min-h-100 border rounded-2xl flex flex-col items-center justify-center p-6 md:p-10 text-center relative shadow-lg"
                     style={{ backgroundColor: 'var(--accent-bg)', borderColor: 'var(--accent-border)' }}>

                    <h3 className="text-xl md:text-2xl font-semibold mb-6" style={{ color: 'var(--text-h)' }}>
                        {currentCard.text}
                    </h3>

                    {!showAnswer && currentCard.questionType === 'MULTIPLE_CHOICE' && (
                        <div className="flex flex-col gap-3 w-full max-w-md">
                            {currentCard.answers.map(ans => (
                                <div key={ans.id} className="p-3 border rounded-lg text-sm md:text-base shadow-sm" style={{ borderColor: 'var(--border)', color: 'var(--text)', backgroundColor: 'var(--bg)' }}>
                                    {ans.text}
                                </div>
                            ))}
                        </div>
                    )}

                    {!showAnswer && currentCard.questionType === 'MATCHING' && (
                        <div className="flex flex-col md:flex-row w-full gap-4 md:gap-6 text-left">
                            <div className="flex-1 flex flex-col gap-2">
                                <h4 className="font-bold text-xs uppercase text-gray-500 mb-1">Pojmy</h4>
                                {leftColumn.map(ans => (
                                    <div key={ans.id} className="p-3 border rounded-lg text-sm md:text-base shadow-sm" style={{ borderColor: 'var(--border)', color: 'var(--text)', backgroundColor: 'var(--bg)' }}>
                                        {ans.text}
                                    </div>
                                ))}
                            </div>
                            <div className="flex-1 flex flex-col gap-2">
                                <h4 className="font-bold text-xs uppercase text-gray-500 mb-1 mt-4 md:mt-0">Definice (Zamíchané)</h4>
                                {shuffledRightColumn.map(ans => (
                                    <div key={ans.id} className="p-3 border rounded-lg text-sm md:text-base shadow-sm" style={{ borderColor: 'var(--border)', color: 'var(--text)', backgroundColor: 'var(--bg)' }}>
                                        {ans.text}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {showAnswer && (
                        <div className="mt-6 md:mt-8 pt-6 md:pt-8 border-t w-full" style={{ borderColor: 'var(--border)' }}>
                            {['WRITE', 'NUMERIC'].includes(currentCard.questionType) && (
                                <div className="text-lg md:text-xl" style={{ color: 'var(--text)' }}>
                                    {currentCard.answers.map(a => <p key={a.id}>{a.text}</p>)}
                                </div>
                            )}

                            {currentCard.questionType === 'MULTIPLE_CHOICE' && (
                                <div className="flex flex-col gap-3 w-full max-w-md mx-auto">
                                    <p className="text-sm uppercase text-gray-500 mb-1 font-bold">Správná odpověď:</p>
                                    {currentCard.answers.filter(a => a.payload?.isCorrect).map(ans => (
                                        <div key={ans.id} className="p-4 border-2 border-green-500 bg-green-50 text-green-900 rounded-xl font-medium text-base md:text-lg shadow-sm">
                                            {ans.text}
                                        </div>
                                    ))}
                                </div>
                            )}

                            {currentCard.questionType === 'MATCHING' && (
                                <div className="flex flex-col gap-3 w-full">
                                    <p className="text-sm uppercase text-gray-500 mb-1 font-bold">Správné dvojice:</p>
                                    {leftColumn.map(left => {
                                        const correctRight = currentCard.answers.find(a => a.payload?.associate && a.payload?.matchId === left.payload?.matchId);
                                        return (
                                            <div key={left.id} className="flex flex-col md:flex-row gap-2 md:gap-4 items-center justify-center p-3 rounded-xl bg-green-50 border border-green-200">
                                                <span className="flex-1 text-center md:text-right font-medium text-green-900 text-sm md:text-base">{left.text}</span>
                                                <span className="text-green-600 font-bold px-2 text-lg md:text-xl transform rotate-90 md:rotate-0">↔</span>
                                                <span className="flex-1 text-center md:text-left font-medium text-green-900 text-sm md:text-base">{correctRight?.text}</span>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="mt-6 mb-4 md:mt-10 md:mb-0 w-full flex justify-center">
                    {!showAnswer ? (
                        <button
                            onClick={() => setShowAnswer(true)}
                            className="px-8 py-3 w-full md:w-auto rounded-full font-bold text-lg transition-transform active:scale-95 shadow-md"
                            style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}
                        >
                            Zobrazit odpověď
                        </button>
                    ) : (
                        <div className="flex flex-wrap justify-center gap-2 md:gap-4 w-full">
                            {user ? (
                                <>
                                    <button onClick={() => handleRating('AGAIN')} className="flex-1 md:flex-none px-4 md:px-6 py-3 md:py-2 rounded-lg font-bold bg-red-100 text-red-700 hover:bg-red-200 transition-colors text-sm md:text-base">Znovu</button>
                                    <button onClick={() => handleRating('HARD')} className="flex-1 md:flex-none px-4 md:px-6 py-3 md:py-2 rounded-lg font-bold bg-orange-100 text-orange-700 hover:bg-orange-200 transition-colors text-sm md:text-base">Těžké</button>
                                    <button onClick={() => handleRating('GOOD')} className="flex-1 md:flex-none px-4 md:px-6 py-3 md:py-2 rounded-lg font-bold bg-green-100 text-green-700 hover:bg-green-200 transition-colors text-sm md:text-base">Dobré</button>
                                    <button onClick={() => handleRating('EXCELLENT')} className="flex-1 md:flex-none px-4 md:px-6 py-3 md:py-2 rounded-lg font-bold bg-blue-100 text-blue-700 hover:bg-blue-200 transition-colors text-sm md:text-base">Výborné</button>
                                </>
                            ) : (
                                <button onClick={() => handleRating('GOOD')} className="px-10 py-3 rounded-full font-bold text-lg transition-transform active:scale-95 shadow-md bg-blue-500 text-white hover:bg-blue-600">
                                    Další kartička
                                </button>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}