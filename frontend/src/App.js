import React, { useState, useEffect } from "react";
import './App.css';

function App() {
  // useState -> 화면에 변하는 데이터 관리.
  // topTradedData : 서버(Spring boot)에서 받아온 순위 리스트를 담는 배열.
  // selectedDate : 사용자가 달력에서 선택한 날짜 값입니다.
  // errorMessage : 서버 통신 실패 시 사용자에게 보여줄 안내 문구입니다.
  const [topTradedData, setTopTradedData] = useState([]); // Top 10 데이터 저장할 상태
  const [selectedDate, setSelectedDate] = useState(''); // 선택된 날짜 저장할 상태
  const [errorMessage, setErrorMessage] = useState(''); // 에러 메시지 저장할 상태

  // 컴포넌트가 마운트 될 때 로기 데이터 로드
  // 페이지가 처음 열릴 때 실행되는 로직.
  useEffect(() => {
    // 오늘 날짜 기본값 설정
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    const defaultDate = `${year}-${month}-${day}`;
    setSelectedDate(defaultDate);

    // 페이지 로드 시 어제 날짜의 데이터 자동 조회(API 기본값이 어제임)
    // 바로 호출해서, 사용자가 버튼을 누르기 전에도 첫 데이터를 화면에 띄워줌.
    fetchTopTradedData(defaultDate);
  }, []); // 빈 배열은 컴포넌트가 처음 마운트될 때 한 번만 실행됨을 의미

  const fetchTopTradedData = async (dateToFetch) => {
    setErrorMessage('');
    setTopTradedData([]); // 기존 데이터 초기화

    let apiUrl = 'http://localhost:8080/api/daily-top-traded';
    if(dateToFetch) {
      apiUrl += `?date=${dateToFetch}`; // 날짜 파라미터 추가
    }

    try {
      const response = await fetch(apiUrl);
      if(!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setTopTradedData(data);
    } catch (error) {
      console.error('데이터 가져오는 중 오류 발생 : ', error);
      setErrorMessage(`데이터 로드 실패: ${error.message}`);
    }
  };

  // 날짜 선택 변경 핸들러
  const handleDateChange = (event) => {
    setSelectedDate(event.target.value);
  }

  // 조회 버튼 클릭 핸들러
  const handleSearchClick = () => {
    fetchTopTradedData(selectedDate);
  };

  // KRW 통화 형식으로 포맷팅하는 함수
  const formatKrw = (amount) => {
    return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>일별 Top 10 거래 코인</h1>
      </header>
      <div className="container">
        <div className="date-selector">
          <label htmlFor="tradeDate">날짜 선택:</label>
          <input
              type="date"
              id="tradeDate"
              value={selectedDate}
              onChange={handleDateChange}
          />
          <button onClick={handleSearchClick}>조회</button>
        </div>
        {errorMessage && <p className="error-message">{errorMessage}</p>}

        {topTradedData.length === 0 && !errorMessage ? (
            <p className="no-data">선택된 날짜의 Top 10 데이터가 없습니다.</p>
        ) : (
            <table>
              <thead>
                <tr>
                  <th>순위</th>
                  <th>코인 코드</th>
                  <th>총 거래 대금 (KRW)</th>
                </tr>
              </thead>
              <tbody>
              {topTradedData.map((item) => (
                  <tr key={item.id}> {/* 각 행에 고유한 key prop 제공 */}
                    <td>{item.rank}</td>
                    <td>{item.code}</td>
                    <td>{formatKrw(item.totalTradedAmount)}</td>
                  </tr>
              ))}
              </tbody>
            </table>
        )}
      </div>
    </div>
  );
}

export default App;
