package com.studysync.model;

import com.studysync.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests verifying StudyRequest state machine transitions and invariants.
 */
class StudyRequestTest {

    private StudyRequest request;
    private Student sender;
    private Student receiver;

    @BeforeEach
    void setUp() throws Exception {
        sender = new Student(1, "Aarav", "aarav@edu", "hash", "CS");
        receiver = new Student(2, "Diya", "diya@edu", "hash", "IT");
        receiver.addSkill("Java", SkillType.KNOWN);

        request = new StudyRequest();
        request.setRequestId(10);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setSubject("Java");
        request.sendRequest();
    }

    @Test
    @DisplayName("Initial state after sendRequest() must be PENDING")
    void testInitialState() {
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("Valid transition: PENDING -> ACCEPTED")
    void testAcceptFromPending() {
        assertDoesNotThrow(() -> request.acceptRequest());
        assertEquals(RequestStatus.ACCEPTED, request.getStatus());
    }

    @Test
    @DisplayName("Valid transition: PENDING -> REJECTED")
    void testRejectFromPending() {
        assertDoesNotThrow(() -> request.rejectRequest());
        assertEquals(RequestStatus.REJECTED, request.getStatus());
    }

    @Test
    @DisplayName("Invalid transition: Accepting an already accepted request must throw InvalidRequestException")
    void testDoubleAcceptFails() throws Exception {
        request.acceptRequest();
        assertEquals(RequestStatus.ACCEPTED, request.getStatus());

        assertThrows(InvalidRequestException.class, () -> request.acceptRequest());
    }

    @Test
    @DisplayName("Invalid transition: Accepting a rejected request must throw InvalidRequestException")
    void testAcceptRejectedFails() throws Exception {
        request.rejectRequest();
        assertEquals(RequestStatus.REJECTED, request.getStatus());

        assertThrows(InvalidRequestException.class, () -> request.acceptRequest());
    }

    @Test
    @DisplayName("Invalid transition: Rejecting an accepted request must throw InvalidRequestException")
    void testRejectAcceptedFails() throws Exception {
        request.acceptRequest();
        assertEquals(RequestStatus.ACCEPTED, request.getStatus());

        assertThrows(InvalidRequestException.class, () -> request.rejectRequest());
    }

    @Test
    @DisplayName("Invariant: Cannot send study request to yourself")
    void testSelfRequestFails() {
        StudyRequest selfReq = new StudyRequest();
        selfReq.setSender(sender);
        selfReq.setReceiver(sender); // Same student
        selfReq.setSubject("Java");

        assertThrows(InvalidRequestException.class, () -> selfReq.sendRequest());
    }
}
