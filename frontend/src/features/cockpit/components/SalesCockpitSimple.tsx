/**
 * Simplified Sales Cockpit without CSS for debugging
 */
export function SalesCockpitSimple() {
  return (
    <div style={{ padding: '20px', backgroundColor: 'lightblue', minHeight: '100vh' }}>
      <h1 style={{ color: 'black', fontSize: '32px', marginBottom: '20px' }}>
        Sales Cockpit Simple - No CSS
      </h1>
      
      <div style={{ backgroundColor: 'white', padding: '20px', marginBottom: '20px', border: '2px solid black' }}>
        <h2>Column 1: Mein Tag</h2>
        <ul>
          <li>Task 1</li>
          <li>Task 2</li>
          <li>Task 3</li>
        </ul>
      </div>
      
      <div style={{ backgroundColor: 'white', padding: '20px', marginBottom: '20px', border: '2px solid black' }}>
        <h2>Column 2: Fokus-Liste</h2>
        <ul>
          <li>Customer 1</li>
          <li>Customer 2</li>
          <li>Customer 3</li>
        </ul>
      </div>
      
      <div style={{ backgroundColor: 'white', padding: '20px', marginBottom: '20px', border: '2px solid black' }}>
        <h2>Column 3: Aktions-Center</h2>
        <p>Arbeitsbereich für kontextbezogene Aktionen</p>
      </div>
    </div>
  );
}