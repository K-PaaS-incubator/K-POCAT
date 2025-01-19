# K-POCAT Overview
## What is Serbit?

Serbit은 ServiceContainer에 의해 관리되는 특정 종류의 메시지를 처리하도록 설계된 컴포넌트로, 다른 서비스에서 전달되는 메시지를 목적에 따라 처리한다.
여러개의 관련된 Serbit을 이용하여 하나의 Service를 구성할 수 있다.

Serbit은 메시지를 처리하는데 있어서 발행/구독 모델을 사용하며 이는 비동기적으로 마이크로 서비스들 사이에서 메시지를 전파하는 형태에 최적화되어있다.

ServiceContainer는 여러 종류의 서비스로부터 전달되는 메시지를 수신하여 Serbit으로 전달하고 Serbit에서 발송하는 메시지를 실제 목적지로 전달한다.
또한 ServiceContainer는 Serbit과 Serbit에서 사용되는 자원의 생명 주기를 관리한다.

# Serbit 상세
## Serbit Lifecycle
Serbit은 생성, 초기화, 처리, 중단의 생명주기에 따라 관리된다.
각 생명주기는 init, serve, destroy API로 표현되며 모든 Serbit 구현은 각 언어에 따라 이 3개의 API를 구현하여야한다.

예를 들어 Java의 경우 Serbit은 다음과 같이 구현된다.
```java
package io.pocat.platform;

public interface Serbit {
    void init(SerbitConfig config) throws UnavailableException;
    void serve(Delivery delivery) throws SerbitException;
    void destroy() throws UnavailableException;
}
```

### 생성
Serbit 인스턴스는 ServiceContainer가 선택한 시점에 생성되어야 한다.
Serbit의 생성은 순서가 보장되지 않는다.
ServiceContainer는 하나의 Serbit에 대해 반드시 하나의 Instance를 유지해야만 하며,
따라서 Serbit의 구현은 반드시 동시성을 보장해야 한다.

#### 인스턴스화 과정의 에러 관리 
인스턴스화 하는 과정에서 에러가 발생할 경우 ServiceContainer는 배치를 중단해야한다.
전체 Serbit 중 하나만 인스턴스화가 실패하더라도 Service는 배치되어서는 안된다.
인스턴스화 과정에서는 자원 요청 등이 발생하지 않으므로 자원 해제 등은 필요없으며 ServiceContainer는 로그 등 적절한 에러 처리를 마치고 중지되어야 한다.

### 초기화
생성 후에 ServiceContainer는 생성된 Serbit들을 초기화해야한다.
ServiceContainer는 Serbit마다 SerbitConfig 객체를 생성하고 init API의 인수로 전달해야 한다.
Serbit은 전달받은 SerbitConfig 객체를 이용하여 ServiceContainer가 설정한 Initial Parameter에 접근할 수 있다.
만약 Serbit이 Service의 공용 자원이나 Context parameter를 사용해야 할 경우 SerbitConfig에 포함된 ServiceContext를 통해 사용할 수 있다. 

모든 Serbit이 초기화되기 전까지 ServiceContainer는 모든 Serbit에 메시지를 전달해서는 안된다.

기본적으로 모든 serbit은 1로 설정된 같은 초기화 우선순위를 갖지만 Service descriptor에 순서를 지정할 수 있다.
serbit 초기화는 우선순위 순서대로 처리되지만 동일한 우선 순위의 경우 순서는 보장되지 않는다. 

#### SerbitConfig
초기화 과정에서 Serbit초기화 API는 SerbitConfig 객체를 전달받는다. 
SerbitConfig 객체는 Serbit의 초기화에 필요한 다음과 같은 정보를 갖는다.
* SerbitId : 배치 과정에서 생성되는 Serbit의 ID이다. 각 Serbit Instance는 고유한 ID를 가진다.  
* SerbitName : Service descriptor에 설정된 Serbit의 이름.
* ServiceContext : Serbit을 포함하고 있는 Service에 대한 정보에 접근할 수 있는 ServiceContext 객체. 더 자세한 내용은 ServiceContext 항목을 참고한다.
* Init parameters : 초기화 Parameter에 관한 정보

#### ServiceContext
ServiceContext는 Serbit이 자신이 속한 서비스에 관한 정보를 얻기 위한 객체이다.
ServiceContext 객체는 ServiceContainer 내에서 유일하며 같은 ServiceContainer에 배치된 모든 Serbit은 같은 ServiceContext 객체를 갖는다.
Serbit은 ServiceContext를 통해 Service의 공유 자원과 Context parameter 등에 접근할 수 있다.
ServiceContext에는 키-값 형태로 속성을 설정할 수 있으며, 한 Serbit에서 설정된 속성은 동일한 ServiceContainer에 포함된 Serbit 간에 공유될 수 있다.
ServiceContext는 다음과 같은 정보를 갖는다.
* ServiceId : ServiceContainer가 시작할 때 생성되는 Service의 ID이다. 배치된 각 Service는 고유한 ID를 가진다.
* ServiceName :  Service descriptor에 설정된 Service의 이름.
* Context parameters : Service 전체에서 사용되는 Parameter 정보
* Resource : Service에 속해있는 serbit에서 사용되는 공유 자원 객체. 자원은 Database Connection 등과 같은 서비스 외부 자원을 의미한다.
* Attributes : Service 내에서 공유될 수 있는 속성값 정보. 읽고 쓰기가 가능하다.

#### 초기화 과정의 에러 관리
만약 Serbit의 초기화 과정에서 에러가 발생할 경우 ServiceContainer는 초기화 과정을 중단하고 적절한 에러 처리를 제공해야 한다.
에러 처리가 완료된 후 ServiceContainer는 대상 Serbit에 대해 다시 초기화를 시도하거나 전체 서비스를 종료할 수 있다.
만약 서비스를 구성하는 Serbit이 하나라도 초기화가 불가능할 경우 전체 서비스는 실행되어서는 안된다.

### 처리
초기화 과정이 완료된 Serbit은 메시지를 수신할 수 있으며 ServiceContainer는 Serbit의 serve API를 호출하여 정상적인 메시지를 전달할 책임을 진다.

전달되는 메시지에 대한 자세한 내용은 MessageDelivery 항목을 참고한다.

#### 처리 과정에서 에러 관리
Serbit이 메시지를 처리하는 중 내부에서 처리하지 못한 모든 에러는 ServiceContainer에게 Exception과 같은 적절한 형태로 전달되어야 한다.
ServiceContainer는 전달된 에러의 에러 코드에 따라 적절한 에러 처리를 제공하고 에러 코드가 없을 경우에도 기본 에러 처리를 제공해야 한다.

### 종료
Serbit은 전체 Service가 종료될 때 종료되어야 하며 개별로 종료될 수 없다.

ServiceContainer는 종료 요청이 발생했을 때 즉시 모든 구독을 중단하며 현재까지 전달된 메시지는 가능한 처리될 때까지 기다리거나 에러 처리를 진행할 수 있다.
모든 Serbit이 더이상 처리하고 있는 메시지가 없을 때 ServiceContainer는 초기화의 역순으로 Serbit의 destroy API를 호출하여 Serbit을 종료한다.

## Message Delivery
Message Delivery는 Serbit에 전달되는 메시지 구조를 의미한다.
Mesage Delivery에는 메시지와 관리 정보가 포함된다.

Message Delivery를 이루고 있는 구성요소는 다음과 같다.

### Envelope
Envelope은 다음과 같은 메시지 관리 정보를 담고 있는 객체이다.
* topic : Message의 Topic을 의미한다. 
* replyTo : Message가 응답이 필요할 경우 응답을 전달할 경로를 포함한다. 단 Serbit은 응답을 보내지 않고 ReplyTo 정보를 그대로 전달하는 메시지에 포함시킬 수 있다.
* Transaction ID : Message가 속해있는 Transaction의 ID이다. 
* Process ID : Message를 처리하고 있는 Process의 ID이다.
* Previous Process ID : Message를 전달한 Process의 ID이다.
* Message ID : 현재 Message의 ID를 의미한다. Message ID는 메시지가 생성될 때마다 Unique한 값이 생성된다.
* timestamp : Message가 생성된 시간.
* Hop Count : Message의 Hop Count. 현재 Transaction 안에서 Message가 전달 가능한 최대수를 의미한다.

### Headers
Message의 Header들의 집합을 의미한다.

### Payload
Message의 내용을 의미한다. byte[] 형태로 전달된다.

# Service
Service는 하나 이상의 Serbit으로 구성된 Serbit의 집합이다.
각 ServiceContainer는 단 하나의 Service만을 관리해야한다.

Service는 다음과 같은 구조로 구성된다.
* **/SVC-INF/service.xml** : 서비스의 구조를 기술하는 서비스 기술자
* **/SVC-INF/libs/** : 서비스에서 사용하는 라이브러리 파일을 포함하는 디렉토리
* **/SVC-INF/classes** 서비스에서 사용하는 클래스 파일을 포함하는 디렉토리
* **/SVC-INF/scripts** 서비스에서 사용하는 스크립트 파일을 포함하는 디렉토리 

## Service Descriptor
Service Descriptor는 Service의 구조를 정의하는 XML 형태의 파일이다.
Service Descriptor는 다음과 같은 항목을 포함한다.

* Service Name : 서비스의 이름
* Serbit 목록 : Service를 구성하는 Serbit의 목록. Serbit 이름, Serbit 초기화 우선도, Serbit class, Serbit initial parameter 등을 포함한다.
* context params : service context에 포함되는 Parameter들의 목록
* resource 목록 : service에서 사용되는 모든 자원의 목록

### Service Descriptor Schema
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="service-descriptor" type="serviceDescriptorType"/>

    <xs:complexType name="serviceDescriptorType">
        <xs:sequence>
            <xs:element name="name" type="xs:string" minOccurs="1" maxOccurs="1"/>
            <xs:element name="serbits" type="serbitsType" minOccurs="1" maxOccurs="1"/>
            <xs:element name="context-params" type="contextParamsType" minOccurs="0" maxOccurs="1"/>
            <xs:element name="resource-refs" type="resourceRefsType" minOccurs="0" maxOccurs="1"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="serbitsType">
        <xs:sequence>
            <xs:element name="serbit" type="serbitType" minOccurs="1" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="serbitType">
        <xs:sequence>
            <xs:element name="serbit-name" type="xs:string" minOccurs="1" maxOccurs="1"/>
            <xs:choice minOccurs="1" maxOccurs="1">
                <xs:element name="serbit-class" type="xs:string"/>
                <xs:element name="serbit-script" type="xs:string"/>
            </xs:choice>
            <xs:element name="init-params" type="initParamsType" minOccurs="0" maxOccurs="1"/>
            <xs:element name="description" type="xs:string" minOccurs="0" maxOccurs="1"/>
        </xs:sequence>
        <xs:attribute name="order" type="xs:positiveInteger" default="1" use="optional" />
    </xs:complexType>

    <xs:complexType name="resourceRefsType">
        <xs:sequence>
            <xs:element name="ref" type="resourceRefType" minOccurs="1" maxOccurs="unbounded" />
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="resourceRefType">
        <xs:attribute name="name" type="xs:string" use="required"/>
        <xs:attribute name="type" type="xs:string" use="required"/>
        <xs:attribute name="description" type="xs:string" use="optional"/>
    </xs:complexType>

    <xs:complexType name="initParamsType">
        <xs:sequence>
            <xs:element name="init-param" type="paramType" minOccurs="1" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="contextParamsType">
        <xs:sequence>
            <xs:element name="context-param" type="paramType" minOccurs="1" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="paramType">
        <xs:attribute name="name" type="xs:string" use="required"/>
        <xs:attribute name="default-value" type="xs:string" use="required"/>
        <xs:attribute name="description" type="xs:string" use="optional"/>
    </xs:complexType>

</xs:schema>
```

### Service Descriptor 예시
```xml
<service-descriptor>
  <name>HelloService</name>
  <serbits>
    <serbit order="1">
      <serbit-name>HelloSerbit</serbit-name>
      <serbit-class>io.pocat.service.HelloSerbit</serbit-class>
      <init-params>
        <init-param name="locale" default-value="en" description="Greetings locale"/>
      </init-params>
      <description>HelloSerbit in English</description>
    </serbit>
    <serbit order="2">
      <serbit-name>SalutSerbit</serbit-name>
      <serbit-class>io.pocat.service.HelloSerbit</serbit-class>
      <init-params>
        <init-param name="locale" default-value="fr" description="Greetings locale"/>
      </init-params>
      <description>HelloSerbit in French</description>
    </serbit>
  </serbits>
  <context-params>
    <context-param name="default-locale" default-value="en" description="Default locale if locale does not set."/>
  </context-params>
  <resource-refs>
    <ref name="greetingDatabase" type="javax,sql.DataSource" description="Greetings database"/>
  </resource-refs>
</service-descriptor>
```
