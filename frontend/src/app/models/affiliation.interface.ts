/** HTTP contract mirror of backend `AffiliationResponse` (JSON). */
export interface AffiliationDto {
  id: number;
  fullName: string;
  email: string;
  reason: string | null;
  createdAt: string;
}
