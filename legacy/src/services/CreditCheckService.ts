/**
 * Credit Check Service
 * Handles customer credit checks via Allianz Trade API
 */


export interface CreditCheckRequest {
  companyName: string;
  legalForm: string;
  registrationNumber?: string;
  vatId: string;
  address: {
    street: string;
    postalCode: string;
    city: string;
    country: string;
  };
  industry: string;
  companySize?: string;
  expectedVolume: number;
  contactPerson: {
    name: string;
    position?: string;
    email: string;
    phone: string;
  };
}

export interface CreditCheckResponse {
  status: 'approved' | 'rejected' | 'review' | 'error';
  creditLimit?: number;
  rating?: number; // 1-10 scale (1 = excellent, 10 = insolvent)
  ratingText?: string;
  message?: string;
  timestamp: Date;
}

export interface ManagementApprovalRequest {
  customerData: CreditCheckRequest;
  reason: string;
  requestedBy: string;
  requestedLimit?: number;
}

export class CreditCheckService {
  private static instance: CreditCheckService;
  
  private constructor() {
    // In production, API configuration would come from environment variables
    // Currently using mock implementation
  }
  
  static getInstance(): CreditCheckService {
    if (!CreditCheckService.instance) {
      CreditCheckService.instance = new CreditCheckService();
    }
    return CreditCheckService.instance;
  }
  
  /**
   * Perform credit check for a customer
   */
  async checkCredit(customer: CreditCheckRequest): Promise<CreditCheckResponse> {
    try {
      // In production, this would be a real API call to Allianz Trade
      // For now, we'll simulate the response
      
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // Simulate credit check logic
      const isNewCustomer = !customer.registrationNumber;
      const volume = customer.expectedVolume;
      
      // Simple mock logic for demo
      if (volume < 5000) {
        return {
          status: 'rejected',
          rating: 8,
          ratingText: 'Unzureichendes Auftragsvolumen',
          message: 'Mindestbestellwert von 5.000€ nicht erreicht',
          timestamp: new Date()
        };
      }
      
      if (isNewCustomer && volume > 50000) {
        return {
          status: 'review',
          rating: 5,
          ratingText: 'Prüfung erforderlich',
          creditLimit: 25000,
          message: 'Neukunde mit hohem Volumen - Geschäftsleitung muss entscheiden',
          timestamp: new Date()
        };
      }
      
      if (volume <= 25000) {
        return {
          status: 'approved',
          rating: 3,
          ratingText: 'Gute Bonität',
          creditLimit: volume,
          message: 'Automatisch genehmigt',
          timestamp: new Date()
        };
      }
      
      return {
        status: 'approved',
        rating: 2,
        ratingText: 'Sehr gute Bonität',
        creditLimit: volume * 1.2, // 20% buffer
        message: 'Kreditlimit bewilligt',
        timestamp: new Date()
      };
      
    } catch (error) {
      console.error('Credit check failed:', error);
      return {
        status: 'error',
        message: 'Bonitätsprüfung fehlgeschlagen. Bitte versuchen Sie es später erneut.',
        timestamp: new Date()
      };
    }
  }
  
  /**
   * Request management approval for a customer
   */
  async requestManagementApproval(request: ManagementApprovalRequest): Promise<{success: boolean; message: string}> {
    try {
      // In production, this would send an email or create a ticket
      // For now, we'll simulate the request
      
      // Create approval request record
      const approvalRequest = {
        id: `MGMT-${Date.now()}`,
        customerData: request.customerData,
        reason: request.reason,
        requestedBy: request.requestedBy,
        requestedLimit: request.requestedLimit,
        status: 'pending',
        createdAt: new Date()
      };
      
      // In a real app, this would be sent to a backend
      console.log('Management approval request:', approvalRequest);
      
      // Simulate sending notification
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      return {
        success: true,
        message: 'Anfrage wurde an die Geschäftsleitung gesendet. Sie erhalten eine Antwort innerhalb von 24 Stunden.'
      };
      
    } catch (error) {
      console.error('Management approval request failed:', error);
      return {
        success: false,
        message: 'Fehler beim Senden der Anfrage. Bitte versuchen Sie es erneut.'
      };
    }
  }
  
  /**
   * Convert rating number to description
   */
  getRatingDescription(rating: number): {text: string; color: string} {
    if (rating <= 2) return { text: 'Sehr gut', color: 'success' };
    if (rating <= 4) return { text: 'Gut', color: 'success' };
    if (rating <= 6) return { text: 'Befriedigend', color: 'warning' };
    if (rating <= 8) return { text: 'Ausreichend', color: 'warning' };
    return { text: 'Mangelhaft', color: 'danger' };
  }
  
  /**
   * Check if customer needs special payment terms
   */
  needsSpecialPaymentTerms(creditResponse: CreditCheckResponse): boolean {
    return creditResponse.status === 'rejected' || 
           creditResponse.status === 'review' ||
           (creditResponse.rating !== undefined && creditResponse.rating > 6);
  }
  
  /**
   * Get recommended payment terms based on credit check
   */
  getRecommendedPaymentTerms(creditResponse: CreditCheckResponse): string {
    if (creditResponse.status === 'rejected') {
      return 'Nur Vorkasse oder Barzahlung';
    }
    
    if (creditResponse.status === 'review') {
      return 'Vorkasse bis zur Freigabe durch Geschäftsleitung';
    }
    
    if (creditResponse.rating) {
      if (creditResponse.rating <= 3) return '30 Tage netto';
      if (creditResponse.rating <= 6) return '14 Tage netto';
      return 'Nur Vorkasse oder Barzahlung';
    }
    
    return 'Nach Vereinbarung';
  }
}

export default CreditCheckService;