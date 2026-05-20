// FoodieGo - small UX helpers
(function(){
    // auto dismiss toast
    document.querySelectorAll('.toast-pop').forEach(function(el){
        setTimeout(function(){ el.style.opacity='0'; el.style.transform='translateY(20px)'; }, 3500);
        setTimeout(function(){ el.remove(); }, 4000);
    });
    // qty buttons
    document.querySelectorAll('[data-qty-btn]').forEach(function(b){
        b.addEventListener('click', function(){
            var input = document.querySelector(b.dataset.qtyTarget);
            if (!input) return;
            var v = parseInt(input.value || '1', 10);
            v += parseInt(b.dataset.qtyBtn, 10);
            if (v < 1) v = 1;
            input.value = v;
        });
    });
    // confirm delete
    document.querySelectorAll('form[data-confirm]').forEach(function(f){
        f.addEventListener('submit', function(e){
            if (!confirm(f.dataset.confirm)) e.preventDefault();
        });
    });
})();

// ===== Card & expiry auto-formatting (added v2) =====
document.addEventListener('input', function(e){
    var t = e.target;
    if (!t || !t.name) return;
    if (t.name === 'cardNumber'){
        var v = t.value.replace(/\D/g,'').slice(0,19);
        t.value = v.replace(/(\d{4})(?=\d)/g,'$1 ').trim();
    } else if (t.name === 'cardExpiry'){
        var v = t.value.replace(/\D/g,'').slice(0,4);
        if (v.length >= 3) v = v.slice(0,2) + '/' + v.slice(2);
        t.value = v;
    } else if (t.name === 'cardCvv'){
        t.value = t.value.replace(/\D/g,'').slice(0,4);
    } else if (t.name === 'phone'){
        t.value = t.value.replace(/[^\d+\-\s]/g,'');
    }
});

// ===== Stricter validation (added v3) =====
// Real-time formatting + on-submit validation. UI/theme untouched.
(function(){
    function setMsg(input, msg){
        if (!input) return;
        var holder = input.parentNode.querySelector('.validation-msg');
        if (!holder){
            holder = document.createElement('div');
            holder.className = 'validation-msg small mt-1';
            holder.style.color = '#FF8B8B';
            input.parentNode.appendChild(holder);
        }
        holder.textContent = msg || '';
    }

    function digits(v){ return (v||'').replace(/\D/g,''); }

    // Enforce input constraints in real-time
    document.addEventListener('input', function(e){
        var t = e.target;
        if (!t || !t.name) return;
        if (t.name === 'phone'){
            // numbers only, max 10
            t.value = digits(t.value).slice(0,10);
            if (t.value.length === 0) setMsg(t,'Phone is required');
            else if (t.value.length !== 10) setMsg(t,'Phone must be exactly 10 digits');
            else setMsg(t,'');
        } else if (t.name === 'cardNumber'){
            var v = digits(t.value).slice(0,16);
            t.value = v.replace(/(\d{4})(?=\d)/g,'$1 ').trim();
            if (v.length === 0) setMsg(t,'Card number is required');
            else if (v.length !== 16) setMsg(t,'Card number must be 16 digits');
            else setMsg(t,'');
        } else if (t.name === 'cardExpiry'){
            var v = digits(t.value).slice(0,4);
            if (v.length >= 3) v = v.slice(0,2) + '/' + v.slice(2);
            t.value = v;
            var msg = validateExpiry(t.value);
            setMsg(t, msg);
        } else if (t.name === 'cardCvv'){
            t.value = digits(t.value).slice(0,3);
            if (t.value.length && t.value.length !== 3) setMsg(t,'CVV must be 3 digits');
            else setMsg(t,'');
        }
    });

    function validateExpiry(val){
        if (!val) return 'Expiry required';
        var m = /^(\d{2})\/(\d{2})$/.exec(val);
        if (!m) return 'Use MM/YY format';
        var mm = parseInt(m[1],10), yy = parseInt(m[2],10);
        if (mm < 1 || mm > 12) return 'Month must be 01-12';
        var now = new Date();
        var curYY = now.getFullYear() % 100;
        var curMM = now.getMonth() + 1;
        if (yy < curYY || (yy === curYY && mm < curMM)) return 'Card is expired';
        if (yy > curYY + 20) return 'Year out of range';
        return '';
    }

    // Block non-digit keys for numeric fields
    document.addEventListener('keypress', function(e){
        var t = e.target;
        if (!t || !t.name) return;
        var numeric = ['phone','cardNumber','cardExpiry','cardCvv'];
        if (numeric.indexOf(t.name) === -1) return;
        var ch = e.key;
        if (ch && ch.length === 1 && !/[0-9]/.test(ch)){
            e.preventDefault();
        }
    });

    // Submit-time validation
    document.addEventListener('submit', function(e){
        var f = e.target;
        if (!f || !f.querySelectorAll) return;
        var ok = true;
        f.querySelectorAll('input[name="phone"]').forEach(function(i){
            if (digits(i.value).length !== 10){ setMsg(i,'Phone must be exactly 10 digits'); ok=false; }
        });
        f.querySelectorAll('input[name="cardNumber"]').forEach(function(i){
            if (digits(i.value).length !== 16){ setMsg(i,'Card number must be 16 digits'); ok=false; }
        });
        f.querySelectorAll('input[name="cardExpiry"]').forEach(function(i){
            var m = validateExpiry(i.value);
            if (m){ setMsg(i,m); ok=false; }
        });
        f.querySelectorAll('input[name="cardCvv"]').forEach(function(i){
            if (digits(i.value).length !== 3){ setMsg(i,'CVV must be 3 digits'); ok=false; }
        });
        if (!ok) e.preventDefault();
    });
})();
