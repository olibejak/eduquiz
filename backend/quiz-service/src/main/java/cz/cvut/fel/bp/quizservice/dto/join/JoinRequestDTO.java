package cz.cvut.fel.bp.quizservice.dto.join;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JoinRequestDTO(
        UUID userId,
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 2, max = 15, message = "Nickname must be between 2 and 15 characters")
        String nickname,

        // PRO-TIP: Identifikátor zařízení
        // Vygeneruje si ho frontend (např. UUID uložené v LocalStorage).
        // Pokud hráč omylem zavře prohlížeč a připojí se znovu se stejným PINem
        // a deviceId, backend pozná: "Aha, to je pořád ten samý hráč!"
        // a nevygeneruje mu nové skóre od nuly.
        String deviceId
) {}
