
        // 1. 브랜드 기획 관련 전역 데이터
        // 브랜드기획 동기화 - 무드태그 선택지에 따라 하위 스타일을 분류하는 매핑 데이터
        const HIERARCHY_DATA_SOURCE = {
            ACTIVE: ["dynamic", "speed", "intense", "power", "race", "energy"],
            CALM: ["wellness", "soft", "mindful", "minimal", "balance", "relaxed"],
            PERFORMANCE: ["running", "training", "trail", "outdoor", "athlete", "sport"],
            LIFESTYLE: ["street", "fashion", "casual", "urban", "daily", "stylish"]
        };

        // 사용자의 선택 실시간으로 저장
        let selectedMainMoods = [];  // 사용자가 선택한 메인 무드
        let selectedSubKeywordsList = [];  // 사용자가 선택한 세부 키워드
        let currentLiveSessionAssets = ["도전 (Challenge)", "러닝 (Running)", "새벽기상 루틴", "도시 중심 (Urban)", "미니멀 (Minimal)", "퍼포먼스 러닝웨어", "Black + White", "통기성 메쉬 (Mesh)"]; // 현재 기획 중인 프로젝트 자산 리스트(지금은 화면 보여주기용이라 나중에 삭제)


        // 2. 브랜드기획 동기화 - 무드태그 태그 로직 (상반태그 비활성화 & 이미지 6개/12개 동적 연동)
        function handleMoodTagClick(clickedTag, opposingTag) {
            const currentBtn = document.getElementById('main-tag-' + clickedTag);
            const oppoBtn = document.getElementById('main-tag-' + opposingTag);

            if(currentBtn.classList.contains('disabled-tag')) return;

            if(currentBtn.classList.contains('selected-tag')) {
                currentBtn.classList.remove('selected-tag');
                oppoBtn.classList.remove('disabled-tag');
                selectedMainMoods = selectedMainMoods.filter(t => t !== clickedTag);
            } else {
                currentBtn.classList.add('selected-tag');
                oppoBtn.classList.add('disabled-tag');
                oppoBtn.classList.remove('selected-tag');
                selectedMainMoods.push(clickedTag);
                const keywordsToClear = HIERARCHY_DATA_SOURCE[opposingTag];
                selectedSubKeywordsList = selectedSubKeywordsList.filter(item => !keywordsToClear.includes(item));
            }
            syncRenderQ4SubClusterGrid();
        }


        // 세부 키워드 그리드 생성
        function syncRenderQ4SubClusterGrid() {
            const poolContainer = document.getElementById('q4-dynamic-style-pool');
            poolContainer.innerHTML = "";

            if (selectedMainMoods.length === 0) {
                poolContainer.innerHTML = `<div style="grid-column: span 4; text-align: center; color: var(--text-gray); padding: 30px; font-size: 13px;">Q3 무드태그를 선택하시면 상응하는 하위 계층 핵심 이미지셋이 실시간으로 여기에 전개됩니다.</div>`;
                return;
            }

            selectedMainMoods.forEach(clusterKey => {
                const subItems = HIERARCHY_DATA_SOURCE[clusterKey];
                subItems.forEach(keywordValue => {
                    const isChosen = selectedSubKeywordsList.includes(keywordValue);
                    const card = document.createElement('div');
                    card.className = `dynamic-style-card ${isChosen ? 'chosen-style' : ''}`;
                    card.setAttribute('data-keyword', keywordValue);

                    card.innerHTML = `
                        <div class="card-img-placeholder">[PREVIEW: ${clusterKey}_${keywordValue}]</div>
                        <div style="text-align:center; font-weight:700; font-size:13px;">${keywordValue}</div>
                        <div style="text-align:center; font-size:10px; color:var(--text-gray); margin-top:2px;">${clusterKey} 계층 소속</div>
                    `;
                    card.onclick = function() { handleSubCardSelection(this, keywordValue); };
                    poolContainer.appendChild(card);
                });
            });
            enforceQ4LockingLimit();
        }


        // 브랜드 기획 동기화 (Q1~Q4 모두 입력 후 다음 단계 이동 가능)
        function checkInputsAndNavigate() {
              // 1. Q1, Q2 텍스트 입력값 체크
              const q1 = document.getElementById('brand-line-summary').value.trim();
              const q2 = document.getElementById('brand-object-search').value.trim();

              if (q1 === "" || q2 === "") {
                  alert("⚠️ Q1과 Q2의 내용을 모두 입력해주세요.");
                  return;
              }

              // 2. Q3 무드태그 선택 체크 (selectedMainMoods 배열에 값이 있는지 확인)
              if (selectedMainMoods.length === 0) {
                  alert("⚠️ Q3에서 최소 1개의 브랜드 무드태그를 선택해주세요.");
                  return;
              }

              // 3. Q4 세부 키워드 선택 체크 (selectedSubKeywordsList 배열에 값이 있는지 확인)
              if (selectedSubKeywordsList.length === 0) {
                  alert("⚠️ Q4에서 최소 1개의 세부 스타일 키워드를 선택해주세요.");
                  return;
              }

              // 모두 통과하면 다음 페이지로 이동
              navigateTo(7);
          }


        // 스타일 선택 및 4개 제한 로직
        function handleSubCardSelection(cardElement, keywordValue) {
            if (cardElement.classList.contains('disabled-style')) return;

            if (cardElement.classList.contains('chosen-style')) {
                cardElement.classList.remove('chosen-style');
                selectedSubKeywordsList = selectedSubKeywordsList.filter(k => k !== keywordValue);
            } else {
                if (selectedSubKeywordsList.length > 4) {
                    alert("⚠️ 세부 스타일 지표 키워드는 최대 4개까지만 복합 바인딩이 허용됩니다.");
                    return;
                }
                cardElement.classList.add('chosen-style');
                selectedSubKeywordsList.push(keywordValue);
            }
            enforceQ4LockingLimit();
        }

        function enforceQ4LockingLimit() {
            const cards = document.querySelectorAll('.dynamic-style-card');
            if (selectedSubKeywordsList.length >= 4) {
                cards.forEach(card => {
                    const kw = card.getAttribute('data-keyword');
                    if (!selectedSubKeywordsList.includes(kw)) card.classList.add('disabled-style');
                });
            } else {
                cards.forEach(card => card.classList.remove('disabled-style'));
            }
        }


        // 레퍼런스 이미지 선택 - 중복선택, 최대 4개 제한
        function selectSingleReference(card, rowId) {
            const rowContainer = document.getElementById(rowId);

            if (card.classList.contains('chosen')) {
                card.classList.remove('chosen');
                return;
            }

            const selectedCountInThisRow = rowContainer.querySelectorAll('.scroll-card.chosen').length;

            if (selectedCountInThisRow >= 4) {
                alert("⚠️ 한 카테고리당 최대 4개까지만 선택할 수 있습니다.");
                return;
            }

            card.classList.add('chosen');
        }


        // 캐러셀 페이지 이동 (이전/다음 화살표)
        function moveCarousel(rowId, direction) {
            const wrapper = document.getElementById(`carousel-${rowId}`);
            const pages = wrapper.querySelectorAll('.carousel-page');
            const dots = wrapper.parentElement.querySelectorAll('.dot');

            // 현재 활성화된 페이지 인덱스 찾기
            let currentIndex = Array.from(pages).findIndex(p => p.classList.contains('active'));

            // 이동할 인덱스 계산
            let nextIndex = currentIndex + direction;

            // 범위 체크 (첫 페이지보다 작거나 마지막 페이지보다 크면 무시)
            if (nextIndex < 0 || nextIndex >= pages.length) return;

            // 페이지 전환
            pages[currentIndex].classList.remove('active');
            pages[nextIndex].classList.add('active');

            // 하단 도트 상태 전환
            dots[currentIndex].classList.remove('active');
            dots[nextIndex].classList.add('active');
        }

        // 도트 클릭 시 특정 페이지로 이동
        function goToPage(rowId, pageIndex) {
            const wrapper = document.getElementById(`carousel-${rowId}`);
            const pages = wrapper.querySelectorAll('.carousel-page');
            const dots = wrapper.parentElement.querySelectorAll('.dot');

            // 현재 활성 상태 해제
            wrapper.querySelector('.carousel-page.active').classList.remove('active');
            wrapper.parentElement.querySelector('.dot.active').classList.remove('active');

            // 선택한 페이지 활성
            pages[pageIndex].classList.add('active');
            dots[pageIndex].classList.add('active');
        }


        // 유효성 검사 후 무드보드 빌드
        // 1. 선택된 모든 데이터를 수집하는 함수
        function validateAndBuildMoodboard() {
            let incompleteCategories = [];
            organizedDataGlobal = [];

            for (let i = 1; i <= 8; i++) {
                const row = document.getElementById(`scroll-row-${i}`);
                if (!row) continue;

                const selectedCards = row.querySelectorAll('.scroll-card.chosen');
                const categoryBlock = row.closest('.category-scroll-block');
                const categoryName = categoryBlock ? categoryBlock.getAttribute('data-cat-name') : `카테고리 ${i}`;

                if (selectedCards.length === 0) {
                    incompleteCategories.push(`${i}번 [${categoryName}]`);
                } else {
                    let assets = [];
                    selectedCards.forEach(card => {
                        assets.push(card.innerText.trim());
                    });

                    organizedDataGlobal.push({ category: categoryName, assets: assets });
                }
            }

            if (incompleteCategories.length > 0) {
                alert(`⚠️ 다음 카테고리에서 최소 1개 이상 선택해주세요:\n- ${incompleteCategories.join('\n- ')}`);
                return;
            }

            // 3. 묶인 데이터를 전달하여 무드보드 생성
            compileMoodboardCanvasHTML(organizedDataGlobal);
            navigateTo(8);
        }