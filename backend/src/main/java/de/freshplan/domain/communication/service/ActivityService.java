package de.freshplan.domain.communication.service;

import de.freshplan.domain.communication.entity.Activity;
import de.freshplan.domain.communication.entity.EntityType;
import de.freshplan.domain.customer.entity.Customer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unified Activity Service
 *
 * <p>Sprint 2.1.7.2 (D8.4): Unified Communication System
 *
 * <p><b>Features:</b>
 *
 * <ul>
 *   <li>Create activities for Lead OR Customer
 *   <li>List activities by entity (Lead/Customer)
 *   <li>✨ <b>Unified timeline</b> (includes Lead→Customer history!)
 * </ul>
 *
 * <p><b>THE KILLER FEATURE:</b> {@link #getCustomerActivitiesIncludingLeadHistory(UUID)}
 *
 * <p>When viewing a Customer that was converted from a Lead:
 *
 * <ul>
 *   <li>✅ Shows activities from Lead phase (entity_type = LEAD, badge: "Als Lead erfasst")
 *   <li>✅ Shows activities from Customer phase (entity_type = CUSTOMER)
 *   <li>✅ Merged timeline sorted by date DESC
 * </ul>
 *
 * <p><b>CRM Best Practice:</b> Salesforce, HubSpot, Dynamics all provide unified timelines!
 */
@ApplicationScoped
public class ActivityService {

  /**
   * Get Activities for Entity
   *
   * <p><b>IMPORTANT:</b> entity_id is String (not UUID!) due to Lead/Customer type mismatch:
   *
   * <ul>
   *   <li>Lead.id = BIGINT → String "123"
   *   <li>Customer.id = UUID → String "550e8400-e29b-..."
   * </ul>
   *
   * @param entityType LEAD or CUSTOMER
   * @param entityId Lead-ID or Customer-ID (as String)
   * @return List of Activities (sorted by date DESC)
   */
  public List<Activity> getActivities(EntityType entityType, String entityId) {
    return Activity.find(
            "entityType = ?1 AND entityId = ?2 ORDER BY activityDate DESC", entityType, entityId)
        .list();
  }

  /**
   * Get Activities for Customer (🎯 THE KILLER FEATURE!)
   *
   * <p><b>Unified Timeline:</b> When Customer was converted from Lead:
   *
   * <ul>
   *   <li>Step 1: Load Customer activities (entity_type = CUSTOMER)
   *   <li>Step 2: Check if customer.originalLeadId exists
   *   <li>Step 3: Load Lead activities (entity_type = LEAD, entity_id = originalLeadId)
   *   <li>Step 4: Merge + sort by date DESC
   * </ul>
   *
   * <p><b>Frontend Use:</b> CustomerDetailPage Activity-Tab shows badge "Als Lead erfasst" for Lead
   * activities.
   *
   * @param customerId Customer UUID
   * @return Unified Timeline (Lead + Customer Activities)
   * @throws NotFoundException if Customer not found
   */
  public List<Activity> getCustomerActivitiesIncludingLeadHistory(UUID customerId) {
    // 1. Find Customer
    Customer customer = Customer.findById(customerId);
    if (customer == null) {
      throw new NotFoundException("Customer not found: " + customerId);
    }

    // 2. Get Customer Activities (entity_type = CUSTOMER, entity_id = customerId)
    List<Activity> activities =
        new ArrayList<>(getActivities(EntityType.CUSTOMER, customerId.toString()));

    // 3. If converted from Lead → include Lead Activities!
    if (customer.getOriginalLeadId() != null) {
      // Lead.id is BIGINT → convert to String
      String leadIdString = customer.getOriginalLeadId().toString();

      List<Activity> leadActivities = getActivities(EntityType.LEAD, leadIdString);
      activities.addAll(leadActivities);

      // Re-sort by date DESC (merge both lists)
      activities.sort((a, b) -> b.activityDate.compareTo(a.activityDate));

      Log.infof(
          "✅ Unified Timeline: Customer %s includes %d Lead activities (originalLeadId=%d)",
          customerId, leadActivities.size(), customer.getOriginalLeadId());
    } else {
      Log.debugf("Customer %s has no Lead history (originalLeadId=null)", customerId);
    }

    return activities;
  }

  /**
   * Get Activities for Lead
   *
   * @param leadId Lead ID (BIGINT)
   * @return List of Activities
   */
  public List<Activity> getLeadActivities(Long leadId) {
    return getActivities(EntityType.LEAD, leadId.toString());
  }

  /**
   * Create Activity
   *
   * <p><b>Factory Pattern:</b> Use {@link Activity#forLead(Long, String,
   * de.freshplan.modules.leads.domain.ActivityType, String)} or {@link Activity#forCustomer(UUID,
   * String, de.freshplan.modules.leads.domain.ActivityType, String)} instead!
   *
   * @param activity Activity to persist
   * @return Persisted Activity
   */
  @Transactional
  public Activity createActivity(Activity activity) {
    if (activity.entityType == null) {
      throw new IllegalArgumentException("entityType is required");
    }
    if (activity.entityId == null || activity.entityId.isBlank()) {
      throw new IllegalArgumentException("entityId is required");
    }
    if (activity.activityType == null) {
      throw new IllegalArgumentException("activityType is required");
    }

    activity.persist();

    Log.infof(
        "Created %s activity for %s %s (id=%s)",
        activity.activityType, activity.entityType, activity.entityId, activity.id);

    return activity;
  }

  /**
   * Update Activity
   *
   * <p>Updates editable fields: summary, description, outcome, nextAction, nextActionDate
   *
   * @param activityId Activity UUID
   * @param summary Updated summary (optional)
   * @param description Updated description (optional)
   * @param outcome Updated outcome (optional)
   * @return Updated Activity
   * @throws NotFoundException if Activity not found
   */
  @Transactional
  public Activity updateActivity(
      UUID activityId,
      String summary,
      String description,
      de.freshplan.modules.leads.domain.ActivityOutcome outcome) {
    Activity activity = Activity.findById(activityId);
    if (activity == null) {
      throw new NotFoundException("Activity not found: " + activityId);
    }

    if (summary != null) {
      activity.summary = summary;
    }
    if (description != null) {
      activity.description = description;
    }
    if (outcome != null) {
      activity.outcome = outcome;
    }

    activity.persist();

    Log.infof("Updated activity %s (%s)", activityId, activity.activityType);

    return activity;
  }

  /**
   * Delete Activity
   *
   * @param activityId Activity UUID
   * @throws NotFoundException if Activity not found
   */
  @Transactional
  public void deleteActivity(UUID activityId) {
    Activity activity = Activity.findById(activityId);
    if (activity == null) {
      throw new NotFoundException("Activity not found: " + activityId);
    }

    activity.delete();

    Log.infof("Deleted activity %s (%s)", activityId, activity.activityType);
  }

  /**
   * Count Activities for Entity
   *
   * @param entityType LEAD or CUSTOMER
   * @param entityId Entity ID (as String)
   * @return Count
   */
  public long countActivities(EntityType entityType, String entityId) {
    return Activity.count("entityType = ?1 AND entityId = ?2", entityType, entityId);
  }
}
