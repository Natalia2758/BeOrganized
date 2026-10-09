package com.example.reminder.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.reminder.dto.ReminderResponse;


@Service
public class ReminderSseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Creates a connection for unidirectional event stream
     * @param idUser    an ID of the user. It is assigned to an emitter during creation
     * @return          an SSE emitter instance
     */
    public SseEmitter connect(Long idUser) {
        // Creates a connection - Spring will keep it open (0L -> no timeout)
        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(idUser, emitter);

        emitter.onCompletion(() -> emitters.remove(idUser, emitter));
        emitter.onTimeout(() -> emitters.remove(idUser, emitter));
        emitter.onError(error -> emitters.remove(idUser, emitter));

        return emitter;
    }

    /**
     * Emits a reminder to a user through an event stream
     * @param idUser    an ID of the user that the emitter is assigned with
     * @param reminder  a reminder response DTO to be sent
     * @return          true if reminder was successfully sent, false if an exception occurred
     */
    public boolean sendToUser(Long idUser, ReminderResponse reminder) {
        
        SseEmitter emitter = emitters.get(idUser);

        if (emitter == null) {
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                .name("reminder")
                .data(reminder)
            );

            return true;
        } catch (IOException e) {
            emitters.remove(idUser, emitter);
            emitter.completeWithError(e);

            return false;
        }
    }

}
