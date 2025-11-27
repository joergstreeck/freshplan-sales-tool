package de.freshplan.modules.xentral.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerAddress;
import de.freshplan.domain.customer.entity.CustomerLocation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xentral Address Matcher - Fuzzy-Matching Service for Multi-Location Management.
 *
 * <p>Sprint 2.1.7.7 - D2: Multi-Location Management - Address Matching
 *
 * <p>**Business Context:** Xentral ERP sends delivery addresses in various formats (typos,
 * abbreviations, inconsistencies). This service uses Levenshtein Distance (edit distance) to match
 * incoming Xentral addresses to the correct branch customer.
 *
 * <p>**Algorithm:**
 *
 * <ol>
 *   <li>Normalize Xentral address (lowercase, trim, remove special chars)
 *   <li>Iterate through all child customers (branches) of the parent
 *   <li>Build normalized address from CustomerAddress entity
 *   <li>Calculate Levenshtein similarity score (0.0 = no match, 1.0 = perfect)
 *   <li>Return branch with highest score ≥ 80% threshold
 *   <li>Fallback: Return parent if no match found
 * </ol>
 *
 * <p>**Example:**
 *
 * <pre>{@code
 * Xentral: "NH Hotel München, Maximilianstraße 17, 80539"
 * Branch:  "NH Hotel München, Maximilianstr. 17, 80539 München"
 * → Similarity: 85% → MATCH ✓
 * }</pre>
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@ApplicationScoped
public class XentralAddressMatcher {

  private static final Logger log = LoggerFactory.getLogger(XentralAddressMatcher.class);

  // Similarity threshold: 80% match required (Salesforce/HubSpot best practice)
  private static final double SIMILARITY_THRESHOLD = 0.80;

  private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

  /**
   * Matches a Xentral delivery address to the correct branch customer.
   *
   * <p>Business Logic: - HEADQUARTER customers can have multiple FILIALE (branch) customers - Each
   * branch has a primary shipping address in CustomerAddress entity - Xentral delivery address may
   * have typos/abbreviations/inconsistent formatting - We fuzzy-match to find the correct branch
   * using Levenshtein distance
   *
   * @param xentralDeliveryAddress Raw delivery address from Xentral ERP (e.g. "NH Hotel München,
   *     Maximilianstr. 17, 80539")
   * @param parent Parent HEADQUARTER customer with child branches
   * @return Best matching branch customer (or parent if no match ≥ 80%)
   */
  public Customer matchDeliveryAddress(String xentralDeliveryAddress, Customer parent) {
    log.debug(
        "Matching Xentral address: '{}' for parent: {}",
        xentralDeliveryAddress,
        parent.getCompanyName());

    // Validate inputs
    if (xentralDeliveryAddress == null || xentralDeliveryAddress.isBlank()) {
      log.warn("Empty Xentral address for parent {}, returning parent", parent.getCompanyName());
      return parent;
    }

    List<Customer> branches = parent.getChildCustomers();
    if (branches == null || branches.isEmpty()) {
      log.debug("No branches found for parent {}, returning parent", parent.getCompanyName());
      return parent;
    }

    // Normalize Xentral address for comparison
    String normalizedInput = normalizeAddress(xentralDeliveryAddress);
    log.trace("Normalized Xentral address: '{}'", normalizedInput);

    // Find best matching branch
    Customer bestMatch = null;
    double bestSimilarity = 0.0;

    for (Customer branch : branches) {
      // Build address from branch's primary shipping address
      String branchAddress = buildBranchAddress(branch);
      if (branchAddress == null || branchAddress.isBlank()) {
        log.trace("Branch {} has no address, skipping", branch.getCompanyName());
        continue;
      }

      String normalizedBranch = normalizeAddress(branchAddress);
      log.trace("Comparing with branch {}: '{}'", branch.getCompanyName(), normalizedBranch);

      // Calculate similarity
      double similarity = calculateSimilarity(normalizedInput, normalizedBranch);
      log.trace("Similarity: {}% for branch {}", (int) (similarity * 100), branch.getCompanyName());

      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestMatch = branch;
      }
    }

    // Check threshold
    if (bestSimilarity >= SIMILARITY_THRESHOLD && bestMatch != null) {
      log.info(
          "✓ Address matched: '{}' → {} ({}% similarity)",
          xentralDeliveryAddress, bestMatch.getCompanyName(), (int) (bestSimilarity * 100));
      return bestMatch;
    }

    // Fallback: Parent (no match found)
    log.warn(
        "⚠ No address match found for '{}' (best: {}%), using parent {}",
        xentralDeliveryAddress, (int) (bestSimilarity * 100), parent.getCompanyName());
    return parent;
  }

  /**
   * Builds a normalized full address from a branch customer's primary shipping address.
   *
   * <p>Logic:
   *
   * <ol>
   *   <li>Get main location → primaryShippingAddress (CustomerAddress entity)
   *   <li>Concatenate: street streetNumber, postalCode city
   *   <li>Normalize (lowercase, trim, remove special chars)
   * </ol>
   *
   * @param branch Branch customer (FILIALE)
   * @return Normalized full address or null if no address found
   */
  private String buildBranchAddress(Customer branch) {
    return branch
        .getMainLocation()
        .flatMap(CustomerLocation::getPrimaryShippingAddress)
        .map(this::buildFullAddress)
        .orElse(null);
  }

  /**
   * Builds full address string from CustomerAddress entity.
   *
   * <p>Format: "street streetNumber, postalCode city" Example: "Maximilianstraße 17, 80539 München"
   *
   * @param address CustomerAddress entity
   * @return Full address string (NOT normalized)
   */
  private String buildFullAddress(CustomerAddress address) {
    StringBuilder sb = new StringBuilder();

    // Street + Street Number
    if (address.getStreet() != null && !address.getStreet().isBlank()) {
      sb.append(address.getStreet());
      if (address.getStreetNumber() != null && !address.getStreetNumber().isBlank()) {
        sb.append(" ").append(address.getStreetNumber());
      }
    }

    // Postal Code + City
    if (address.getPostalCode() != null && !address.getPostalCode().isBlank()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(address.getPostalCode());
    }

    if (address.getCity() != null && !address.getCity().isBlank()) {
      if (address.getPostalCode() != null && !address.getPostalCode().isBlank()) {
        sb.append(" ");
      } else if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(address.getCity());
    }

    return sb.toString();
  }

  /**
   * Normalizes address for fuzzy matching.
   *
   * <p>Operations:
   *
   * <ol>
   *   <li>Lowercase
   *   <li>Trim whitespace
   *   <li>Remove special characters (keep only alphanumeric + spaces)
   *   <li>Normalize multiple spaces to single space
   * </ol>
   *
   * <p>Example: "Maximilianstraße 17, 80539 München" → "maximilianstrasse 17 80539 muenchen"
   *
   * @param address Raw address string
   * @return Normalized address for matching
   */
  private String normalizeAddress(String address) {
    if (address == null) {
      return "";
    }

    return address
        .toLowerCase()
        .trim()
        .replaceAll("ä", "ae")
        .replaceAll("ö", "oe")
        .replaceAll("ü", "ue")
        .replaceAll("ß", "ss")
        .replaceAll("[^a-z0-9\\s]", "") // Remove special chars (keep letters, numbers, spaces)
        .replaceAll("\\s+", " "); // Normalize whitespace
  }

  /**
   * Calculates similarity between two addresses using Levenshtein distance.
   *
   * <p>Algorithm: Levenshtein Distance (edit distance) = minimum number of single-character edits
   * (insertions, deletions, substitutions) required to change one word into the other.
   *
   * <p>Similarity Score = 1 - (distance / maxLength)
   *
   * <p>Example: - "maximilianstrasse 17 80539 muenchen" vs "maximilianstr 17 80539 muenchen" -
   * Distance: 6 edits (missing "asse") - maxLength: 36 - Similarity: 1 - (6/36) = 0.833 = 83%
   *
   * @param address1 First normalized address
   * @param address2 Second normalized address
   * @return Similarity score (0.0 = no match, 1.0 = perfect match)
   */
  private double calculateSimilarity(String address1, String address2) {
    int maxLength = Math.max(address1.length(), address2.length());
    if (maxLength == 0) {
      return 1.0; // Both empty = perfect match
    }

    int distance = levenshteinDistance.apply(address1, address2);
    double similarity = 1.0 - ((double) distance / maxLength);

    return Math.max(0.0, Math.min(1.0, similarity)); // Clamp to [0.0, 1.0]
  }
}
