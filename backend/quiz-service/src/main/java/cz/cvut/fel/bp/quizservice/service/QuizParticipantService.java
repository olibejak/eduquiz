package cz.cvut.fel.bp.quizservice.service;

import cz.cvut.fel.bp.quizservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizParticipantService {

    private final QuizParticipantRepository quizParticipantRepository;

    @Transactional(readOnly = true)
    public QuizParticipant findById(@NotNull Long id) {
        return quizParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant with id " + id + " not found"));
    }

    @Transactional
    public QuizParticipant changeConnectionStatus(Long participantId, boolean isConnected) {
        QuizParticipant participant = findById(participantId);
        participant.setIsConnected(isConnected);

        return saveParticipant(participant);
    }

    @Transactional
    public QuizParticipant evaluate(Long participantId, Integer points, boolean isCorrect) {
        QuizParticipant participant = findById(participantId);
        participant.addPoints(points);
        participant.setIsCurrentCorrect(isCorrect);
        return participant;
    }

    @Transactional
    public QuizParticipant saveParticipant(QuizParticipant participant) {
        return quizParticipantRepository.save(participant);
    }

    @Transactional
    public void addPointsAndAdvance(Long participantId, int points) {
        QuizParticipant player = findById(participantId);
        player.setCurrentScore(player.getCurrentScore() + points);
        quizParticipantRepository.save(player);
    }

    @Transactional(readOnly = true)
    public boolean didAlreadyAnswer(Long participantId) {
        QuizParticipant participant = findById(participantId);
        return participant.getIsCurrentCorrect() == null;
    }

    @Transactional(readOnly = true)
    public QuizParticipant findByToken(String token) {
        return quizParticipantRepository.findByToken(token);
    }

    @Transactional
    public void delete(QuizParticipant participant) {
        quizParticipantRepository.delete(participant);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long participantId) {
        return quizParticipantRepository.existsById(participantId);
    }
}