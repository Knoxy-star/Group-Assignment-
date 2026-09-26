import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet';
import './leafletIconFix';
import { SeverityBadge } from './Badges';

const DEFAULT_CENTER = [-16.71, 32.18];

/** Dashboard map: one marker per approved incident, popup on click. */
export default function IncidentMap({ incidents }) {
  const withCoords = incidents.filter((i) => i.latitude != null && i.longitude != null);
  const center = withCoords.length
    ? [withCoords[0].latitude, withCoords[0].longitude]
    : DEFAULT_CENTER;

  return (
    <MapContainer center={center} zoom={9} style={{ height: '420px', width: '100%' }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {withCoords.map((incident) => (
        <Marker key={`${incident.hazardLabel}-${incident.id}`} position={[incident.latitude, incident.longitude]}>
          <Popup>
            <div className="space-y-1 text-sm">
              <div className="font-semibold">{incident.hazardLabel}</div>
              <div>
                {incident.ward}, {incident.district}
              </div>
              <SeverityBadge severity={incident.severity} />
              <div className="text-xs text-slate-500">
                {new Date(incident.occurredAt).toLocaleString()}
              </div>
            </div>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
