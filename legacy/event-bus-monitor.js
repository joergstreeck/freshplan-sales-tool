// Event Bus Monitor - Paste this in browser console
// Usage: Run this after opening the app with ?phase2=true

(function() {
    console.log('🎯 Event Bus Monitor Started');
    
    const events = [
        'customer:saved',
        'customer:cleared', 
        'customer:creditCheckRequired',
        'legacy:skipped',
        'module:customer:*'
    ];
    
    const eventLog = [];
    
    events.forEach(eventPattern => {
        if (eventPattern.includes('*')) {
            // For wildcard patterns, we need a different approach
            const originalDispatch = EventTarget.prototype.dispatchEvent;
            EventTarget.prototype.dispatchEvent = function(event) {
                if (event.type.startsWith(eventPattern.replace('*', ''))) {
                    console.log(`📨 Event: ${event.type}`, event.detail || '');
                    eventLog.push({
                        time: new Date().toISOString(),
                        type: event.type,
                        detail: event.detail
                    });
                }
                return originalDispatch.call(this, event);
            };
        } else {
            window.addEventListener(eventPattern, (e) => {
                console.log(`📨 Event: ${eventPattern}`, e.detail || '');
                eventLog.push({
                    time: new Date().toISOString(),
                    type: eventPattern,
                    detail: e.detail
                });
            });
        }
    });
    
    // Helper functions
    window.eventBusMonitor = {
        getLog: () => eventLog,
        clear: () => { eventLog.length = 0; console.log('Event log cleared'); },
        test: () => {
            console.log('🧪 Dispatching test event...');
            window.dispatchEvent(new CustomEvent('customer:saved', {
                detail: { test: true, timestamp: Date.now() }
            }));
        }
    };
    
    console.log('✅ Monitoring events:', events.join(', '));
    console.log('📋 Commands:');
    console.log('  eventBusMonitor.getLog() - Show all captured events');
    console.log('  eventBusMonitor.clear()  - Clear event log');
    console.log('  eventBusMonitor.test()   - Dispatch test event');
    
})();