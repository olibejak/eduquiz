import { useState } from 'react';
import { api } from '../api/axios';
import { useNavigate } from 'react-router-dom';
import { DECK_TAGS }from '../types/deck';
import type {
    VisibilityType,
    QuestionType,
    QuestionState,
    AnswerState,
    ChoicePayload,
    MatchingPayload
} from '../types/deck';
import {useAuth} from "../context/AuthContext.tsx";

export default function CreateDeck() {

    const { user } = useAuth();

    const navigate = useNavigate();
    const [deckId, setDeckId] = useState<number | null>(null);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [visibility, setVisibility] = useState<VisibilityType>('PUBLIC');
    const [selectedTags, setSelectedTags] = useState<string[]>([]);
    const [questions, setQuestions] = useState<QuestionState[]>([]);

    const toggleTag = (tag: string) => {
        setSelectedTags(prev => prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]);
    };

    const addQuestion = () => {
        setQuestions([...questions, {
            text: '',
            questionType: 'MULTIPLE_CHOICE',
            duration: 30,
            answers: [],
            isLocked: false
        }]);
    };

    const formatQuestionForApi = (q: QuestionState) => {
        return {
            text: q.text,
            questionType: q.questionType,
            duration: q.duration,
            answers: q.answers.map((ans) => {
                if (ans.payload) {
                    return {
                        text: ans.text,
                        type: ans.type,
                        payload: { ...ans.payload, type: ans.type }
                    };
                }

                return {
                    text: ans.text,
                    type: ans.type
                };
            })
        };
    };

    const saveQuestionIncremental = async (index: number) => {
        if (!deckId) return;
        const q = questions[index];
        if (q.text.trim() === '' || q.answers.length === 0) return;

        const formattedQuestion = formatQuestionForApi(q);

        try {
            if (q.id) {
                await api.put(`/decks/${deckId}/questions/${q.id}`, formattedQuestion);
            } else {
                const res = await api.post(`/decks/${deckId}/questions`, formattedQuestion);
                const updatedQuestions = [...questions];
                updatedQuestions[index].id = res.data.id;
                updatedQuestions[index].isLocked = true;
                setQuestions(updatedQuestions);
            }
        } catch (err) {
            console.error("Chyba průběžného ukládání", err);
        }
    };

    const handleDeleteDeck = async () => {
        if (!deckId || !window.confirm("Opravdu smazat celou sadu?")) return;
        try {
            await api.delete(`/decks/${deckId}`); //
            alert("Sada byla smazána.");
            navigate('/library');
        } catch (err) {
            console.error(err);
            alert("Nepodařilo se smazat sadu.");
        }
    };

    const deleteQuestion = async (index: number) => {
        const q = questions[index];
        if (q.id && deckId) {
            try {
                await api.delete(`/decks/${deckId}/questions/${q.id}`);
            } catch (err) {
                console.error("Chyba při mazání otázky", err);
                return;
            }
        }
        setQuestions(questions.filter((_, i) => i !== index));
    };

    const handleFinalSave = async () => {
        try {
            const deckRequest = {
                title,
                description,
                visibility,
                tags: selectedTags,
                questions: questions.map(q => formatQuestionForApi(q))
            };

            if (deckId) {
                await api.put(`/decks/${deckId}`, deckRequest);
            } else {
                const res = await api.post('/decks', deckRequest);
                setDeckId(res.data.id);
            }
            alert("Sada byla úspěšně uložena!");
            navigate('/library');
        } catch (err) {
            console.error(err);
            alert("Chyba při ukládání sady. Ujistěte se, že všechny otázky mají odpovědi.");
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6 space-y-6 rounded-lg shadow-sm"
             style={{ backgroundColor: 'var(--bg)', border: '1px solid var(--border)' }}>

            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold" style={{ color: 'var(--text-h)' }}>Vytvořit novou sadu</h1>
                {deckId && (
                    <button onClick={handleDeleteDeck} className="text-xs px-3 py-1 rounded border hover:opacity-80 transition-opacity" style={{ color: '#ef4444', borderColor: '#ef4444' }}>
                        Smazat sadu
                    </button>
                )}
            </div>

            {/* Deck info*/}
            <div className="space-y-4">
                <input className="w-full p-2 border rounded bg-transparent outline-none focus:border-blue-500" style={{ color: 'var(--text)', borderColor: 'var(--border)' }} placeholder="Název sady" value={title} onChange={e => setTitle(e.target.value)} />
                <textarea className="w-full p-2 border rounded bg-transparent outline-none focus:border-blue-500" style={{ color: 'var(--text)', borderColor: 'var(--border)' }} placeholder="Popis" value={description} onChange={e => setDescription(e.target.value)} />
                <select className="p-2 border rounded bg-transparent outline-none" style={{ color: 'var(--text)', borderColor: 'var(--border)' }} value={visibility} onChange={e => setVisibility(e.target.value as VisibilityType)}>
                    <option value="PUBLIC" style={{background: 'var(--bg)'}}>Veřejná</option>
                    <option value="PRIVATE" style={{background: 'var(--bg)'}}>Soukromá</option>
                </select>
            </div>

            {/* Tags */}
            <div className="space-y-2">
                <h3 className="text-sm font-medium" style={{ color: 'var(--text-h)' }}>Štítky (Tagy)</h3>
                <div className="flex flex-wrap gap-2">
                    {DECK_TAGS.map(tag => {
                        const isVerifiedTag = tag.id === 'VERIFIED';
                        const isAdmin = user?.role === 'ADMIN';

                        // Info: do not show verified tag if not ROLE_ADMIN
                        if (isVerifiedTag && !isAdmin) {
                            return null;
                        }

                        return (
                            <button
                                key={tag.id}
                                onClick={() => toggleTag(tag.id)}
                                className="px-3 py-1 rounded-full text-xs transition-colors border"
                                style={{
                                    backgroundColor: selectedTags.includes(tag.id) ? 'var(--accent)' : 'var(--accent-bg)',
                                    color: selectedTags.includes(tag.id) ? 'var(--bg)' : 'var(--text)',
                                    borderColor: 'var(--accent-border)'
                                }}
                            >
                                {tag.label}
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* Questions */}
            <div className="space-y-4">
                <h2 className="text-xl font-semibold" style={{ color: 'var(--text-h)' }}>Otázky</h2>
                {questions.map((q, index) => (
                    <div key={index} className="p-4 border rounded-md space-y-3" style={{ backgroundColor: 'var(--accent-bg)', borderColor: 'var(--accent-border)' }}>
                        <div className="flex justify-between items-start w-full">
                            <div className="flex gap-4 flex-1">
                                <select
                                    disabled={q.isLocked || q.answers.length > 0} // Info: Lock question type change
                                    className="p-2 border rounded bg-transparent disabled:opacity-50"
                                    style={{ color: 'var(--text)', borderColor: 'var(--border)' }}
                                    value={q.questionType}
                                    onChange={e => {
                                        const newQs = [...questions];
                                        newQs[index].questionType = e.target.value as QuestionType;
                                        setQuestions(newQs);
                                    }}
                                >
                                    <option value="MULTIPLE_CHOICE" style={{background: 'var(--bg)'}}>Výběr z více možností</option>
                                    <option value="WRITE" style={{background: 'var(--bg)'}}>Písemná</option>
                                    <option value="NUMERIC" style={{background: 'var(--bg)'}}>Číselná</option>
                                    <option value="MATCHING" style={{background: 'var(--bg)'}}>Přiřazovací</option>
                                </select>
                                <select
                                    className="p-3 border rounded-xl bg-[var(--bg)] text-sm w-full sm:w-28 outline-none focus:border-[var(--accent)] transition-colors border-[var(--border)] text-[var(--text-h)]"
                                    value={q.duration}
                                    onChange={e => {
                                        const newQs = [...questions];
                                        newQs[index].duration = parseInt(e.target.value);
                                        setQuestions(newQs);
                                    }}
                                >
                                    {[10, 20, 30, 45, 60, 90, 120].map(sec => (
                                        <option key={sec} value={sec} style={{background: 'var(--bg)'}}>{sec}s</option>
                                    ))}
                                </select>
                                <input
                                    className="flex-1 p-2 border rounded bg-transparent outline-none focus:border-blue-500"
                                    style={{ color: 'var(--text)', borderColor: 'var(--border)' }}
                                    placeholder="Text otázky"
                                    value={q.text}
                                    onChange={e => {
                                        const newQs = [...questions];
                                        newQs[index].text = e.target.value;
                                        setQuestions(newQs);
                                    }}
                                    onBlur={() => saveQuestionIncremental(index)}
                                />
                            </div>
                            <button onClick={() => deleteQuestion(index)} className="ml-2 p-2 rounded-md hover:opacity-70 transition-opacity">
                                <span style={{ color: '#ef4444' }}>✕</span>
                            </button>
                        </div>

                        {/* Answers */}
                        <AnswerEditor
                            questionType={q.questionType}
                            answers={q.answers}
                            onChange={(newAnswers) => {
                                const newQs = [...questions];
                                newQs[index].answers = newAnswers;
                                setQuestions(newQs);
                            }}
                        />
                    </div>
                ))}

                <button onClick={addQuestion} className="w-full py-2 rounded font-medium transition-colors border" style={{ backgroundColor: 'var(--code-bg)', color: 'var(--text)', borderColor: 'var(--border)' }}>
                    + Přidat otázku
                </button>
            </div>

            <button onClick={handleFinalSave} className="w-full py-3 rounded-lg font-bold transition-transform active:scale-95" style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}>
                Uložit kompletní sadu
            </button>
        </div>
    );
}

// Answer editor
const AnswerEditor = ({ questionType, answers, onChange }: { questionType: QuestionType, answers: AnswerState[], onChange: (newAnswers: AnswerState[]) => void }) => {

    let maxAnswers = 100;
    if (questionType === 'NUMERIC') maxAnswers = 1;
    else if (questionType === 'WRITE') maxAnswers = 3;
    else if (questionType === 'MULTIPLE_CHOICE') maxAnswers = 6;
    else if (questionType === 'MATCHING') maxAnswers = 8;

    const canAddAnswer = answers.length < maxAnswers;

    const addAnswer = () => {
        if (!canAddAnswer) return;

        let newAnswer: AnswerState;

        if (questionType === 'MULTIPLE_CHOICE') {
            newAnswer = { text: '', type: 'CHOICE', payload: { isCorrect: false } };
        } else if (questionType === 'MATCHING') {
            newAnswer = { text: '', type: 'MATCHING', payload: { associate: false, matchId: 1 } };
        } else {
            newAnswer = { text: '', type: 'STANDARD', payload: null };
        }

        onChange([...answers, newAnswer]);
    };

    const updateAnswer = (index: number, fields: Partial<AnswerState>) => {
        const updated = [...answers];
        updated[index] = { ...updated[index], ...fields };
        onChange(updated);
    };

    const updatePayload = (index: number, payloadFields: Partial<ChoicePayload & MatchingPayload>) => {
        const updated = [...answers];
        const currentPayload = updated[index].payload || {};

        updated[index].payload = { ...currentPayload, ...payloadFields } as ChoicePayload | MatchingPayload;

        onChange(updated);
    };

    const removeAnswer = (index: number) => {
        onChange(answers.filter((_, i) => i !== index));
    };

    return (
        <div className="mt-4 space-y-2 border-t pt-2" style={{ borderColor: 'var(--border)' }}>
            <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                    <h4 className="text-sm font-medium" style={{ color: 'var(--text-h)' }}>Odpovědi</h4>
                    <span className="text-xs opacity-60" style={{ color: 'var(--text)' }}>
                        ({answers.length} / {maxAnswers}{questionType === 'MATCHING' ? ' položek' : ''})
                    </span>
                </div>

                {canAddAnswer && (
                    <button onClick={addAnswer} className="text-xs px-2 py-1 rounded transition-colors" style={{ backgroundColor: 'var(--accent)', color: 'var(--bg)' }}>
                        + Odpověď
                    </button>
                )}
            </div>

            {answers.map((ans, idx) => (
                <div key={idx} className="flex gap-2 items-center p-2 rounded border" style={{ backgroundColor: 'var(--accent-bg)', borderColor: 'var(--accent-border)' }}>

                    <input
                        type={questionType === 'NUMERIC' ? "number" : "text"}
                        className="flex-1 p-1 text-sm bg-transparent outline-none border-b focus:border-blue-500"
                        style={{ color: 'var(--text)', borderColor: 'var(--border)' }}
                        placeholder={questionType === 'WRITE' ? "Zadejte správnou odpověď..." : questionType === 'NUMERIC' ? "Zadejte číslo..." : "Text odpovědi..."}
                        value={ans.text}
                        onChange={e => updateAnswer(idx, { text: e.target.value })}
                    />

                    {questionType === 'MULTIPLE_CHOICE' && (
                        <label className="flex items-center gap-1 text-xs cursor-pointer" style={{ color: 'var(--text)' }}>
                            <input type="checkbox" className="accent-zinc-800" checked={(ans.payload as ChoicePayload).isCorrect} onChange={e => updatePayload(idx, { isCorrect: e.target.checked })} />
                            Správně
                        </label>
                    )}

                    {questionType === 'MATCHING' && (
                        <div className="flex gap-2 items-center">
                            <input type="number" min="1" max="4" className="w-12 p-1 text-xs border rounded bg-transparent" style={{ color: 'var(--text)', borderColor: 'var(--border)' }} value={(ans.payload as MatchingPayload).matchId} onChange={e => updatePayload(idx, { matchId: parseInt(e.target.value) || 1 })} />
                            <select className="text-xs p-1 border rounded bg-transparent" style={{ color: 'var(--text)', borderColor: 'var(--border)' }} value={(ans.payload as MatchingPayload).associate ? 'true' : 'false'} onChange={e => updatePayload(idx, { associate: e.target.value === 'true' })}>
                                <option value="false" style={{background: 'var(--bg)'}}>Pojem</option>
                                <option value="true" style={{background: 'var(--bg)'}}>Definice</option>
                            </select>
                        </div>
                    )}

                    <button onClick={() => removeAnswer(idx)} className="ml-1 text-xs hover:opacity-70 transition-opacity" title="Odstranit odpověď">
                        <span style={{ color: '#ef4444' }}>✕</span>
                    </button>
                </div>
            ))}
        </div>
    );
};