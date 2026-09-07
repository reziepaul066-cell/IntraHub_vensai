package com.intrahub.portal.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = :status AND (l.employee.manager.id = :managerId OR :managerId IS NULL) ORDER BY l.createdAt DESC")
    List<LeaveRequest> findPendingRequestsForManager(@Param("status") LeaveStatus status, @Param("managerId") Long managerId);
}
