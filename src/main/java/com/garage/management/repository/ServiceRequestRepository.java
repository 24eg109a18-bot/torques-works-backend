package com.garage.management.repository;

import com.garage.management.entity.ServiceRequest;
import com.garage.management.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findAllByOrderByIdDesc();

    @Query("select r from ServiceRequest r where r.vehicle.owner.id = :uid order by r.id desc")
    List<ServiceRequest> findByCustomer(@Param("uid") Long customerId);

    @Query("select r from ServiceRequest r where r.mechanic.id = :mid order by r.id desc")
    List<ServiceRequest> findByMechanic(@Param("mid") Long mechanicId);

    @Query("select r from ServiceRequest r where r.vehicle.id = :vid and r.status = :status order by r.id desc")
    List<ServiceRequest> findByVehicleAndStatus(@Param("vid") Long vehicleId, @Param("status") ServiceStatus status);

    @Query("select count(r) > 0 from ServiceRequest r where r.vehicle.id = :vid")
    boolean existsForVehicle(@Param("vid") Long vehicleId);

    long countByStatus(ServiceStatus status);
    long countByStatusIn(Collection<ServiceStatus> statuses);

    @Query("select count(r) from ServiceRequest r where r.mechanic.id = :mid and r.status in :statuses")
    long countByMechanicAndStatusIn(@Param("mid") Long mechanicId, @Param("statuses") Collection<ServiceStatus> statuses);

    /** Double-booking check: is this mechanic already busy at that date/time? */
    @Query("""
           select count(r) > 0 from ServiceRequest r
           where r.mechanic.id = :mid and r.id <> :rid
             and r.appointment.appointmentDate = :date and r.appointment.appointmentTime = :time
             and r.status in :statuses
           """)
    boolean mechanicBusy(@Param("mid") Long mechanicId, @Param("rid") Long requestId,
                         @Param("date") LocalDate date, @Param("time") LocalTime time,
                         @Param("statuses") Collection<ServiceStatus> statuses);

    /** Same vehicle cannot have two open bookings for the same slot. */
    @Query("""
           select count(r) > 0 from ServiceRequest r
           where r.vehicle.id = :vid
             and r.appointment.appointmentDate = :date and r.appointment.appointmentTime = :time
             and r.status not in :closed
           """)
    boolean vehicleBusy(@Param("vid") Long vehicleId, @Param("date") LocalDate date,
                        @Param("time") LocalTime time, @Param("closed") Collection<ServiceStatus> closed);
}
