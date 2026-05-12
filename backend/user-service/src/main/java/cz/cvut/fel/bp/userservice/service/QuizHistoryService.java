package cz.cvut.fel.bp.userservice.service;

import cz.cvut.fel.bp.userservice.dto.quiz.QuizEndedEventDTO;
import cz.cvut.fel.bp.userservice.dto.quiz.QuizLeaderboardDTO;
import cz.cvut.fel.bp.userservice.dto.quiz.UserHistoryItemDTO;
import cz.cvut.fel.bp.userservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.userservice.model.QuizHistory;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserQuizResult;
import cz.cvut.fel.bp.userservice.repository.QuizHistoryRepository;
import cz.cvut.fel.bp.userservice.repository.UserQuizResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizHistoryService {

    private final QuizHistoryRepository quizHistoryRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final UserService userService;

    public List<UserHistoryItemDTO> getUserHistory(UUID userId) {
        return userQuizResultRepository.findAllByUserIdWithQuizData(userId).stream()
                .map(result -> new UserHistoryItemDTO(
                        result.getQuiz().getId(),
                        result.getQuiz().getDeckTitles(),
                        result.getQuiz().getPlayedAt(),
                        result.getScore(),
                        result.getPosition()
                ))
                .toList();
    }

    public QuizLeaderboardDTO getQuizLeaderboard(Long sessionId) {
        QuizHistory quiz = quizHistoryRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz session not found"));

        List<QuizLeaderboardDTO.LeaderboardRowDTO> players = quiz.getResults().stream()
                .map(r -> new QuizLeaderboardDTO.LeaderboardRowDTO(r.getNickname(), r.getScore(), r.getPosition()))
                .toList();

        return new QuizLeaderboardDTO(
                quiz.getId(),
                quiz.getDeckTitles(),
                quiz.getPlayedAt(),
                players
        );
    }

    public void handleQuizEndedEvent(QuizEndedEventDTO event) {
        QuizHistory quizHistory = QuizHistory.builder()
                .playedAt(event.finishedAt())
                .deckTitles(event.deckTitles())
                .build();

        for (QuizEndedEventDTO.UserQuizResultDTO userDTO : event.results()) {
            User user = null;
            if (userDTO.userId() != null) {
                user = userService.getUserById(userDTO.userId());
            }

            UserQuizResult result = UserQuizResult.builder()
                    .user(user)
                    .nickname(userDTO.nickname())
                    .score(userDTO.finalScore())
                    .position(userDTO.position())
                    .build();

            quizHistory.addResult(result);
        }

        quizHistoryRepository.save(quizHistory);
    }
}
