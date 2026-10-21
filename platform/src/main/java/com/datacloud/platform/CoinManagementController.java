package com.datacloud.platform;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coins")
public class CoinManagementController {

    @PersistenceContext
    private EntityManager em;

    private final CoinLimits limits;

    public CoinManagementController(CoinLimits limits) {
        this.limits = limits;
    }

    public record CoinRequest(Integer amount) {}

    @PostMapping("/{idUser}/add")
    @Transactional
    public ResponseEntity<?> addCoins(
            @PathVariable Integer idUser,
            @RequestBody CoinRequest request) {

        if (request == null || request.amount() == null || request.amount() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La cantidad debe ser mayor que cero"));
        }

        UserCoins user = em.find(
                UserCoins.class,
                idUser,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Usuario inexistente"));
        }

        long nuevoSaldo = (long) user.balance + request.amount();

        if (nuevoSaldo > limits.getMaxBalance()) {
            return ResponseEntity.status(409)
                    .body(Map.of(
                            "error", "Se supera el limite maximo de coins",
                            "limite", limits.getMaxBalance(),
                            "saldoActual", user.balance
                    ));
        }

        user.balance = (int) nuevoSaldo;

        return ResponseEntity.ok(Map.of(
                "message", "Coins agregados correctamente",
                "saldo", user.balance
        ));
    }

    @PostMapping("/{idUser}/remove")
    @Transactional
    public ResponseEntity<?> removeCoins(
            @PathVariable Integer idUser,
            @RequestBody CoinRequest request) {

        if (request == null || request.amount() == null || request.amount() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La cantidad debe ser mayor que cero"));
        }

        UserCoins user = em.find(
                UserCoins.class,
                idUser,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Usuario inexistente"));
        }

        if (user.balance < request.amount()) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Saldo insuficiente"));
        }

        user.balance -= request.amount();

        return ResponseEntity.ok(Map.of(
                "message", "Coins retirados correctamente",
                "saldo", user.balance
        ));
    }
}
